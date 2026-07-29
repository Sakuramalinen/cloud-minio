# 文件上传 API 手册

> 面向前端开发者。本文档描述文件上传的完整流程和所有相关接口。

---

## 1. 概述

### 1.1 上传架构

```
前端 → 后端（授权）→ 前端直连 MinIO PUT 文件 → 后端（保存/合并）
```

- 后端负责：授权校验、秒传判断、生成预签名 URL、分片合并、文件记录
- 前端负责：计算文件 MD5、切割分片、通过预签名 URL 直传文件到 MinIO、上报进度

### 1.2 基础信息

| 项目 | 说明 |
|------|------|
| 基础 URL | `http://{host}:{port}`（根据部署环境而定） |
| 认证方式 | 请求 Header 中携带 `authorization` 和 `user-info` |
| 响应格式 | 统一包装为 `Result<T>` |
| 预签名 URL 有效期 | **10 分钟**（超时需重新获取） |

### 1.3 通用响应格式 `Result<T>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": <具体数据，类型取决于接口>
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 表示成功 |
| msg | String | 提示信息 |
| data | T | 业务数据，类型因接口而异，无数据时为 `null` |

### 1.4 重要约定：Long 类型序列化

**后端所有 `Long` 类型（包括 ID）在 JSON 中序列化为字符串。**

原因：后端使用雪花算法生成 ID，数值超过 JavaScript `Number` 的安全整数范围（`2^53 - 1`），会导致精度丢失。

**前端注意：**
- 接收响应时，所有 ID 字段为**字符串**
- 发送请求时，ID 字段**也传字符串**（后端会自动反序列化）

---

## 2. 上传流程总览

### 2.1 完整流程

```
Step 1: 调用「上传授权」接口
           ↓
      拿到 { taskId, isUpload, isChunked, chunkBitmap, chunkSize }
           ↓
    ┌──────┴──────────────────────────┐
    ↓                                  ↓
isUpload = true                   isUpload = false
秒传命中，流程结束                      ↓
                               ┌──────┴──────────┐
                               ↓                  ↓
                        isChunked = false    isChunked = true
                        完整上传流程          分片上传流程
```

### 2.2 完整上传流程（isChunked = false）

```
Step 2: 调用「获取完整上传 URL」→ 拿到预签名 PUT URL
Step 3: 前端 PUT 文件二进制数据到该 URL
Step 4: 调用「保存上传文件」→ 后端记录完成
```

### 2.3 分片上传流程（isChunked = true）

```
Step 2: 根据 chunkBitmap 确定待上传的分片编号
Step 3: 调用「获取分片上传 URL」→ 拿到每片的预签名 PUT URL
Step 4: 逐个 PUT 每片数据到对应 URL，从响应头提取 ETag
        （上传过程中可调用「保存上传进度」上报进度）
Step 5: 全部分片上传完成后，调用「分片合并」→ 传入 ETag 映射
```

> **断点续传**：随时调用「查询上传进度列表」获取上次进度，从未完成的 `chunkBitmap` 位继续。

---

## 3. API 详述

---

### 3.1 上传授权

```
POST /file-transfer/upload/auth
```

**作用**：上传文件前必须调用。后端校验上传资格，判断是否秒传，决定分片策略。

#### 入参（请求体 JSON）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| parentId | Long → String | 是 | 目标文件夹 ID，`"0"` 表示根目录 |
| fileName | String | 是 | 文件名，含扩展名。如 `"年度报告.pdf"` |
| fileMd5 | String | 是 | 文件完整 MD5 值（32 位小写十六进制），前端用 spark-md5 等库计算 |
| fileSize | Long → String | 是 | 文件大小，单位**字节**。如 `"104857600"` 表示 100MB |

#### 请求示例

```json
{
  "parentId": "0",
  "fileName": "年度报告.pdf",
  "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
  "fileSize": "104857600"
}
```

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| taskId | Long → String | **上传任务 ID**，后续所有接口都需要传这个值 |
| isUpload | Boolean | 是否秒传命中。`true` = 文件已存在，直接完成，无需后续上传 |
| isChunked | Boolean | **是否分片上传**。前端根据此字段决定走完整上传还是分片上传流程 |
| chunkSize | Long → String | 每片大小（字节）。`isChunked=false` 时为 `"-1"` |
| chunkBitmap | String | 分片进度位图。"0"=未上传，"1"=已上传。长度 = 总分片数 |

#### 响应示例

**正常授权（需分片上传）：**

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "taskId": "1802345678901234567",
    "isUpload": false,
    "isChunked": true,
    "chunkSize": "5242880",
    "chunkBitmap": "0000000000"
  }
}
```

> 上例中 `chunkBitmap` 为 `"0000000000"`，表示共 10 片，全部未上传。

**正常授权（完整上传）：**

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "taskId": "1802345678901234567",
    "isUpload": false,
    "isChunked": false,
    "chunkSize": "-1",
    "chunkBitmap": ""
  }
}
```

**秒传命中（无需上传）：**

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "taskId": null,
    "isUpload": true,
    "isChunked": null,
    "chunkSize": null,
    "chunkBitmap": null
  }
}
```

> `isUpload=true` 时，其他字段无意义，前端直接提示"上传成功"即可。

#### 可能的错误

| HTTP 状态码 | 场景 | 响应示例 |
|-------------|------|----------|
| 400 | 目录不存在 | `{"code":..., "msg":"该目录不存在", "data":null}` |
| 400 | 同名文件已存在 | `{"code":..., "msg":"该目录下存在同名文件", "data":null}` |

---

### 3.2 获取完整上传 URL

```
POST /file-transfer/upload/direction-connect/whole-file
```

**作用**：获取用于完整上传的预签名 PUT URL。**仅在 `isChunked=false` 时调用。**

#### 入参（请求体）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| （直接传值） | Long → String | 是 | taskId，来自 auth 接口返回值 |

> **注意**：请求体是纯字符串 `"1802345678901234567"`，不是 `{"taskId": "..."}` 对象。

#### 请求示例

```json
"1802345678901234567"
```

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | String | MinIO 预签名 PUT URL |

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": "http://192.168.1.100:9000/my-bucket/original/a/b/d41d8cd98f00b204e9800998ecf8427e.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
}
```

#### 前端上传方式

拿到 URL 后，直接 PUT 文件：

```typescript
const presignedUrl = response.data; // 预签名 URL
const fileBlob = ...; // 文件 Blob/File 对象

await fetch(presignedUrl, {
  method: 'PUT',
  body: fileBlob,
  headers: {
    'Content-Type': file.type, // 如 "application/pdf"
  },
});
```

> 如果 PUT 返回 200，表示上传成功。

#### 可能的错误

| HTTP 状态码 | 场景 |
|-------------|------|
| 400 | 上传任务未找到（taskId 无效或缓存过期） |

---

### 3.3 获取分片上传 URL

```
POST /file-transfer/upload/direction-connect/chunk-file
```

**作用**：获取指定分片的预签名 PUT URL。**仅在 `isChunked=true` 时调用。** 每次可请求多个分片。

#### 入参（请求体 JSON）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| taskId | Long → String | 是 | 上传任务 ID |
| chunkNumbers | Integer[] | 是 | 需要获取 URL 的分片编号列表。**分片编号从 1 开始** |

#### 请求示例

```json
{
  "taskId": "1802345678901234567",
  "chunkNumbers": [1, 2, 3]
}
```

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | Map\<Integer, String\> | 分片编号 → 预签名 PUT URL |

> Key 为 Integer，Value 为预签名 URL 字符串。

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "1": "http://192.168.1.100:9000/my-bucket/original/a/b/d41d8c...?partNumber=1&uploadId=xxx&X-Amz-...",
    "2": "http://192.168.1.100:9000/my-bucket/original/a/b/d41d8c...?partNumber=2&uploadId=xxx&X-Amz-...",
    "3": "http://192.168.1.100:9000/my-bucket/original/a/b/d41d8c...?partNumber=3&uploadId=xxx&X-Amz-..."
  }
}
```

#### 前端上传方式

```typescript
const urlMap = response.data; // { "1": "http://...", "2": "http://...", "3": "http://..." }
const chunkBlob = ...; // 文件分片 Blob

const uploadResult = await fetch(urlMap["1"], {
  method: 'PUT',
  body: chunkBlob,
});

// 从响应头中提取 ETag（后续合并时需要）
const etag = uploadResult.headers.get('ETag') || '';
// ETag 格式通常是 "d41d8cd98f00b204e9800998ecf8427e"（带引号）
// 传给后端时需要保持原样
```

#### 业务说明

- 建议一次请求 3~5 个分片 URL，并发上传
- 每片 PUT 成功后，**务必从响应头 `ETag` 中提取值**，合并时需要
- URL 有效期 10 分钟，超时需重新获取
- 已上传过的分片不需要重复获取 URL（通过 chunkBitmap 判断）

#### 可能的错误

| HTTP 状态码 | 场景 |
|-------------|------|
| 400 | 上传任务未找到 |

---

### 3.4 保存完整上传文件

```
POST /file-transfer/upload/save
```

**作用**：完整上传（非分片）完成后调用，通知后端文件已上传，后端记录到数据库。**仅完整上传流程使用。**

#### 入参（请求体）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| （直接传值） | Long → String | 是 | taskId |

> 请求体为纯字符串，与 3.2 接口一致。

#### 请求示例

```json
"1802345678901234567"
```

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | null | 无返回数据 |

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

#### 可能的错误

| HTTP 状态码 | 场景 |
|-------------|------|
| 400 | 上传任务未找到 |

---

### 3.5 合并分片文件

```
POST /file-transfer/upload/merge
```

**作用**：所有分片上传完成后调用，通知后端合并分片并记录文件。**仅分片上传流程使用。**

#### 入参（请求体 JSON）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| taskId | Long → String | 是 | 上传任务 ID |
| parts | Map\<Integer, String\> | 是 | 分片编号 → ETag 的映射。**Key 为 Integer，Value 为 String** |

#### 请求示例

```json
{
  "taskId": "1802345678901234567",
  "parts": {
    "1": "\"d41d8cd98f00b204e9800998ecf8427e\"",
    "2": "\"e51d8cd98f00b204e9800998ecf8427f\"",
    "3": "\"f61d8cd98f00b204e9800998ecf84280\""
  }
}
```

> ETag 值需要保持从 PUT 响应头中获取的**原始格式（含引号）**。

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | null | 无返回数据 |

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

#### 业务说明

- 必须**所有分片全部上传完成后**才能调用
- `parts` 中需要包含**每一个**分片的 ETag，不能遗漏
- 合并成功后文件即正式可用

#### 可能的错误

| HTTP 状态码 | 场景 |
|-------------|------|
| 400 | 上传任务未找到 |

---

### 3.6 保存上传进度

```
POST /upload-task-record/save
```

**作用**：上传过程中保存进度。用于断点续传、暂停恢复。分片上传过程中可选调用。

#### 入参（请求体 JSON）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| taskId | Long → String | 是 | 上传任务 ID |
| chunkBitMap | String | 否 | 当前分片进度位图。如 `"11000"` 表示第 1、2 片已完成 |
| status | Integer | 否 | 上传状态：`0`=排队等待中，`1`=已暂停，`2`=网络中断 |

#### 请求示例

**上报进度（正在上传中）：**

```json
{
  "taskId": "1802345678901234567",
  "chunkBitMap": "11000",
  "status": 0
}
```

**用户主动暂停：**

```json
{
  "taskId": "1802345678901234567",
  "chunkBitMap": "11000",
  "status": 1
}
```

**网络中断：**

```json
{
  "taskId": "1802345678901234567",
  "chunkBitMap": "11000",
  "status": 2
}
```

#### 出参（data 字段）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | null | 无返回数据 |

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

#### 业务说明

- 建议每上传完一个分片就调用一次
- `status=1` 用于用户主动暂停，下次通过 list 接口恢复
- `status=2` 用于网络断开时保存进度

---

### 3.7 查询上传进度列表

```
GET /upload-task-record/list
```

**作用**：查询当前用户所有未完成的上传任务。用于断点续传——获取上次进度后从未完成的分片继续。

#### 入参

无（从认证 Header 获取当前用户）

#### 出参（data 为数组，每个元素如下）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| taskId | Long → String | 任务 ID |
| parentId | Long → String | 目标文件夹 ID |
| fileMd5 | String | 文件 MD5 值 |
| fileName | String | 文件名 |
| uploadId | String | 上传标识 |
| status | Integer | 上传状态：`0`=排队等待中，`1`=已暂停，`2`=网络中断 |
| uploadType | Integer | 上传方式：`0`=完整上传，`1`=分片上传 |
| chunkSize | Long → String | 每片大小（字节） |
| fileSize | Long → String | 文件大小（字节） |
| chunkBitmap | String | 分片进度位图 |
| expireTime | String | 任务过期时间，格式 `"yyyy-MM-dd HH:mm:ss"` |
| createTime | String | 创建时间，格式 `"yyyy-MM-dd HH:mm:ss"` |
| updateTime | String | 更新时间，格式 `"yyyy-MM-dd HH:mm:ss"` |

#### 响应示例

```json
{
  "code": 200,
  "msg": "ok",
  "data": [
    {
      "taskId": "1802345678901234567",
      "parentId": "0",
      "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
      "fileName": "年度报告.pdf",
      "uploadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "status": 0,
      "uploadType": 1,
      "chunkSize": "5242880",
      "fileSize": "104857600",
      "chunkBitmap": "11000",
      "expireTime": "2026-08-03 12:00:00",
      "createTime": "2026-07-27 10:30:00",
      "updateTime": "2026-07-27 10:35:00"
    }
  ]
}
```

#### 业务说明

- 返回当前用户所有状态不为"已完成"的任务
- 通过 `chunkBitmap` 判断哪些分片未上传（值为 `"0"` 的位置）
- 通过 `uploadType` 判断是完整上传还是分片上传
- 通过 `status` 判断任务是否被暂停

---

## 4. 前端代码示例（TypeScript）

### 4.1 类型定义

```typescript
// ===== 通用响应 =====
interface Result<T> {
  code: number;
  msg: string;
  data: T;
}

// ===== 上传授权 =====
interface UploadAuthRequest {
  parentId: string;   // Long → String
  fileName: string;
  fileMd5: string;
  fileSize: string;   // Long → String
}

interface UploadAuthResponse {
  taskId: string | null;      // Long → String
  isUpload: boolean;
  isChunked: boolean | null;
  chunkSize: string | null;   // Long → String
  chunkBitmap: string | null;
}

// ===== 分片上传 =====
interface ChunkFileRequest {
  taskId: string;              // Long → String
  chunkNumbers: number[];      // 分片编号从 1 开始
}

// 响应 data 为 Record<number, string>

// ===== 分片合并 =====
interface MergeRequest {
  taskId: string;              // Long → String
  parts: Record<number, string>; // 分片编号 → ETag
}

// ===== 进度保存 =====
interface ProgressSaveRequest {
  taskId: string;              // Long → String
  chunkBitMap?: string;
  status?: number;             // 0=等待中, 1=已暂停, 2=网络中断
}

// ===== 上传任务记录 =====
interface UploadTaskRecord {
  taskId: string;              // Long → String
  parentId: string;            // Long → String
  fileMd5: string;
  fileName: string;
  uploadId: string;
  status: number;
  uploadType: number;          // 0=完整上传, 1=分片上传
  chunkSize: string;           // Long → String
  fileSize: string;            // Long → String
  chunkBitmap: string;
  expireTime: string;
  createTime: string;
  updateTime: string;
}
```

### 4.2 计算文件 MD5

```typescript
import SparkMD5 from 'spark-md5';

function computeFileMd5(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const chunkSize = 2 * 1024 * 1024; // 2MB 每片读取
    const chunks = Math.ceil(file.size / chunkSize);
    const spark = new SparkMD5.ArrayBuffer();
    const reader = new FileReader();
    let currentChunk = 0;

    reader.onload = (e) => {
      if (e.target?.result instanceof ArrayBuffer) {
        spark.append(e.target.result);
        currentChunk++;
        if (currentChunk < chunks) {
          loadNext();
        } else {
          resolve(spark.end());
        }
      }
    };

    reader.onerror = () => reject(new Error('文件读取失败'));

    function loadNext() {
      const start = currentChunk * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      reader.readAsArrayBuffer(file.slice(start, end));
    }

    loadNext();
  });
}
```

### 4.3 完整上传（小文件）

```typescript
async function uploadSmallFile(file: File, parentId: string): Promise<void> {
  // 1. 计算 MD5
  const md5 = await computeFileMd5(file);

  // 2. 上传授权
  const authRes = await fetch('/file-transfer/upload/auth', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      parentId,
      fileName: file.name,
      fileMd5: md5,
      fileSize: String(file.size),
    }),
  });
  const authData: Result<UploadAuthResponse> = await authRes.json();
  if (authData.code !== 200) throw new Error(authData.msg);

  const { taskId, isUpload, isChunked } = authData.data;

  // 3. 秒传判断
  if (isUpload) {
    console.log('秒传成功');
    return;
  }

  // 4. 完整性校验
  if (isChunked) {
    throw new Error('需要使用分片上传');
  }

  // 5. 获取完整上传 URL
  const urlRes = await fetch('/file-transfer/upload/direction-connect/whole-file', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(taskId),  // 直接传 taskId 字符串
  });
  const urlData: Result<string> = await urlRes.json();
  if (urlData.code !== 200) throw new Error(urlData.msg);

  // 6. PUT 文件到预签名 URL
  const uploadRes = await fetch(urlData.data, {
    method: 'PUT',
    body: file,
    headers: { 'Content-Type': file.type },
  });
  if (!uploadRes.ok) throw new Error('上传失败');

  // 7. 保存文件记录
  const saveRes = await fetch('/file-transfer/upload/save', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(taskId),
  });
  const saveData = await saveRes.json();
  if (saveData.code !== 200) throw new Error(saveData.msg);

  console.log('上传完成');
}
```

### 4.4 分片上传（大文件，含断点续传）

```typescript
async function uploadLargeFile(
  file: File,
  parentId: string,
  resumeTaskId?: string  // 断点续传时传入上次的 taskId
): Promise<void> {
  // ===== 1. 如果没有 taskId，先授权 =====
  let taskId: string;
  let chunkSize: number;
  let chunkBitmap: string;
  let totalChunks: number;

  if (resumeTaskId) {
    // 断点续传：从进度列表获取
    const listRes = await fetch('/upload-task-record/list');
    const listData: Result<UploadTaskRecord[]> = await listRes.json();
    const task = listData.data.find(t => t.taskId === resumeTaskId);
    if (!task) throw new Error('任务不存在或已过期');
    taskId = task.taskId;
    chunkSize = Number(task.chunkSize);
    chunkBitmap = task.chunkBitmap;
    totalChunks = chunkBitmap.length;
  } else {
    // 新上传：计算 MD5 并授权
    const md5 = await computeFileMd5(file);

    const authRes = await fetch('/file-transfer/upload/auth', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        parentId,
        fileName: file.name,
        fileMd5: md5,
        fileSize: String(file.size),
      }),
    });
    const authData: Result<UploadAuthResponse> = await authRes.json();
    if (authData.code !== 200) throw new Error(authData.msg);

    if (authData.data.isUpload) {
      console.log('秒传成功');
      return;
    }
    if (!authData.data.isChunked) {
      throw new Error('文件太小，请使用完整上传');
    }

    taskId = authData.data.taskId!;
    chunkSize = Number(authData.data.chunkSize);
    chunkBitmap = authData.data.chunkBitmap!;
    totalChunks = chunkBitmap.length;
  }

  // ===== 2. 找出未上传的分片 =====
  const pendingChunks: number[] = [];
  for (let i = 0; i < chunkBitmap.length; i++) {
    if (chunkBitmap[i] === '0') {
      pendingChunks.push(i + 1); // 分片编号从 1 开始
    }
  }

  if (pendingChunks.length === 0) {
    console.log('所有分片已上传，可直接合并');
    return;
  }

  // ===== 3. 分批上传分片 =====
  const BATCH_SIZE = 5; // 每批并发数
  const parts: Record<number, string> = {};

  for (let i = 0; i < pendingChunks.length; i += BATCH_SIZE) {
    const batch = pendingChunks.slice(i, i + BATCH_SIZE);

    // 3.1 获取该批分片的预签名 URL
    const urlRes = await fetch('/file-transfer/upload/direction-connect/chunk-file', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taskId, chunkNumbers: batch }),
    });
    const urlData: Result<Record<number, string>> = await urlRes.json();
    if (urlData.code !== 200) throw new Error(urlData.msg);

    // 3.2 并发上传该批分片
    const uploadPromises = batch.map(async (chunkNumber) => {
      const start = (chunkNumber - 1) * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      const chunkBlob = file.slice(start, end);

      const presignedUrl = urlData.data[chunkNumber];
      const res = await fetch(presignedUrl, {
        method: 'PUT',
        body: chunkBlob,
      });

      if (!res.ok) throw new Error(`分片 ${chunkNumber} 上传失败`);

      // 提取 ETag
      const etag = res.headers.get('ETag') || '';
      parts[chunkNumber] = etag;

      // 更新位图
      chunkBitmap = chunkBitmap.substring(0, chunkNumber - 1) + '1' + chunkBitmap.substring(chunkNumber);

      // 3.3 上报进度
      await fetch('/upload-task-record/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          taskId,
          chunkBitMap: chunkBitmap,
          status: 0,
        }),
      });
    });

    await Promise.all(uploadPromises);
  }

  // ===== 4. 合并分片 =====
  const mergeRes = await fetch('/file-transfer/upload/merge', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ taskId, parts }),
  });
  const mergeData = await mergeRes.json();
  if (mergeData.code !== 200) throw new Error(mergeData.msg);

  console.log('分片上传完成');
}
```

---

## 附录

### A. 上传状态枚举（status）

| 值 | 含义 |
|:--|------|
| 0 | 排队等待中 |
| 1 | 已暂停 |
| 2 | 网络中断 |

### B. 上传方式枚举（uploadType）

| 值 | 含义 |
|:--|------|
| 0 | 完整上传 |
| 1 | 分片上传 |

### C. 常见错误场景

| 场景 | 可能原因 |
|------|----------|
| 上传任务未找到 | taskId 无效，或缓存已过期（10 分钟），需重新调用 auth |
| 该目录不存在 | parentId 无效 |
| 该目录下存在同名文件 | 目标目录已有同名文件，需改名或删除 |

### D. 预签名 URL 说明

- 预签名 URL 是 MinIO 生成的临时授权 URL，前端直接用 `PUT` 方法上传文件二进制数据
- 有效期为 **10 分钟**，超时需重新调用接口获取
- 分片上传的 URL 包含 `partNumber` 和 `uploadId` 参数，无需前端手动拼接
- PUT 请求成功后，响应头 `ETag` 即为该片的标识，合并时需要用到
