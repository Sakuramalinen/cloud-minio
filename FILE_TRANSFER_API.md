# 文件传输模块 API 文档

> 面向前端开发人员。本文档覆盖文件上传、下载、预览的全部 HTTP API。

---

## 0. 前置说明

### 0.1 基础信息

| 项目 | 说明 |
|---|---|
| 基础路径 | `/file-transfer` |
| 请求方式 | 所有请求体为 `application/json`（GET 请求使用 Query 参数） |
| 认证方式 | 通过 Cookie / Header 传递登录态，后端自动识别当前用户 |

### 0.2 统一响应格式 — `Result<T>`

所有非分页接口均返回此结构：

```json
{
  "code": 200,
  "msg": "ok",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `Integer` | 状态码，`200` 表示成功 |
| `msg` | `String` | 提示信息 |
| `data` | 泛型 `T` | 实际业务数据，各接口不同 |

### 0.3 分页请求参数 — `PageParams`

分页接口（缩略图列表）使用以下 Query 参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| `pageSize` | `Long` | 否 | `10` | 每页条数 |
| `pageNo` | `Long` | 否 | `1` | 当前页码（从 1 开始） |
| `isAsc` | `Boolean` | 否 | `true` | 是否升序 |
| `sortBy` | `String` | 否 | — | 排序字段名 |

### 0.4 分页响应格式 — `PageResult<T>`

分页接口返回此结构：

```json
{
  "total": 100,
  "pageSize": 10,
  "pageNo": 1,
  "items": [ ... ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | `Long` | 总记录数 |
| `pageSize` | `Long` | 每页大小 |
| `pageNo` | `Long` | 当前页码 |
| `items` | `List<T>` | 当前页数据列表 |

### 0.5 关于预签名 URL

上传、下载、预览接口均**不传输文件本体**，而是返回一个 **MinIO 预签名 URL**。前端拿到后：

- **上传**：对预签名 URL 发起 `PUT` 请求，body 为文件二进制
- **下载/预览**：对预签名 URL 发起 `GET` 请求

每个 URL 都有有效期（上传/预览 10 分钟，下载 5 分钟），过期需重新请求。

---

## 1. 上传文件

### 1.1 完整流程

上传有两条路径，取决于文件是否需要分片。建议根据文件大小自行判断。

#### 路径 A：整文件上传（小文件，如 < 10MB）

```
前端                                      后端                                     MinIO
 │                                         │                                        │
 │──① POST /upload/auth ──────────────────→│                                        │
 │  请求体: { parentId, fileName,          │  校验权限、检查秒传、生成 uploadId        │
 │            fileMd5, fileSize,           │  存入 Redis 缓存                       │
 │            isSlice: false }             │                                        │
 │←──{ isUpload: false, uploadId: "xxx" }─│                                        │
 │                                         │                                        │
 │──② POST /upload/direction-connect/whole-file ──→│                               │
 │  请求体: "xxx" (uploadId)               │  从 Redis 获取上传上下文                │
 │←──{ data: "https://minio/...?sign=xx" }│  生成预签名 PUT URL                     │
 │                                         │                                        │
 │──③ PUT https://minio/...?sign=xx ──────────────────────────────────────────────→│
 │  请求体: 文件二进制                      │                                        │  文件写入 MinIO
 │←──200 OK ───────────────────────────────────────────────────────────────────────│
 │                                         │                                        │
 │──④ POST /upload/save ─────────────────→│                                        │
 │  请求体: "xxx" (uploadId)               │  从 Redis 获取上下文                    │
 │←──{ code: 200 }───────────────────────│  写入 file_object + user_file 表         │
 │                                         │  删除 Redis 缓存                       │
 ✅ 完成
```

**关键点**：
- 步骤③ 是前端直连 MinIO，**不经过后端**
- 步骤④ 的作用是通知后端"我上传完了，请记录到数据库"
- 如果 ① 返回 `isUpload: true`，说明秒传命中，跳过 ②③④

#### 路径 B：分片上传（大文件）

```
前端                                      后端                                     MinIO
 │                                         │                                        │
 │──① POST /upload/auth ──────────────────→│                                        │
 │  请求体: { ..., isSlice: true }         │  初始化 MinIO 分片上传任务               │
 │←──{ isUpload: false, uploadId: "xxx" }─│  获取真实 uploadId（MinIO 返回）         │
 │                                         │                                        │
 │──② POST /upload/direction-connect/chunk-file ──→│                               │
 │  请求体: { uploadId: "xxx",             │  从 Redis 获取上下文                    │
 │            chunkNumbers: [1,2,3,...] }  │  为每个分片生成预签名 PUT URL            │
 │←──{ data: { 1: "url1", 2: "url2", ... }}│                                       │
 │                                         │                                        │
 │──③ 并发 PUT 各分片 URL ─────────────────────────────────────────────────────────→│
 │  对每个 chunkNumber 分别 PUT            │                                        │  分片写入 MinIO
 │←──200 OK（记录每个响应的 ETag 头）──────│                                        │
 │                                         │                                        │
 │──④ POST /upload/merge ────────────────→│                                        │
 │  请求体: {                               │                                        │
 │    uploadId: "xxx",                     │                                        │
 │    parts: { 1: "etag1", 2: "etag2" }   │  调用 MinIO 合并分片                    │
 │  }                                       │  写入 file_object + user_file 表        │
 │←──{ code: 200 }───────────────────────│  删除 Redis 缓存                        │
 │                                         │                                        │
 ✅ 完成
```

**关键点**：
- 分片上传的 `uploadId` 是 MinIO 返回的真实值，**不是后端生成的 UUID**
- 步骤③ 中每个分片 PUT 后，**必须记录响应头 `ETag` 的值**，在步骤④ 传入
- 步骤④ 会同时完成文件合并 + 数据库写入，**不需要额外调 `/upload/save`**

---

### 1.2 `POST /file-transfer/upload/auth` — 上传授权

**上传前必须先调此接口**，获取上传授权。支持秒传判断。

#### 请求体 — `UploadAuthorizationDTO`

```json
{
  "parentId": 0,
  "fileName": "报告.pdf",
  "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
  "fileSize": 1048576,
  "isSlice": false
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `parentId` | `Long` | ✅ 是 | 上传到哪个目录。**根目录传 `0`** |
| `fileName` | `String` | ✅ 是 | 文件名（含扩展名），如 `"报告.pdf"` |
| `fileMd5` | `String` | ✅ 是 | 文件 MD5 值（32 位小写十六进制），用于秒传判断 |
| `fileSize` | `Long` | ✅ 是 | 文件大小，单位：字节 |
| `isSlice` | `Boolean` | ✅ 是 | 是否分片上传。`false` = 整文件上传，`true` = 分片上传 |

> **注意**：当前端计算 MD5 可能较耗时，建议对大文件使用 Web Worker 或 `spark-md5` 增量计算。

#### 响应体 — `Result<UploadFileVO>`

**① 需要实际上传：**

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "isUpload": false,
    "uploadId": "550e8400-e29b-41d4-a716-446655440000",
    "urls": null
  }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `isUpload` | `Boolean` | 是否已完成上传。`false` = 需要继续上传 |
| `uploadId` | `String` | 本次上传的会话 ID，后续步骤必须携带 |
| `urls` | `Map<Integer, String>` | 此阶段为 `null`，分片上传 URL 由下一步获取 |

**② 秒传命中（无需上传）：**

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "isUpload": true,
    "uploadId": null,
    "urls": null
  }
}
```

- `isUpload: true` 表示服务器已有相同 MD5 的文件，**已自动完成上传，无需后续步骤**
- `uploadId` 和 `urls` 此时为 `null`

#### 异常情况

| 场景 | 错误信息 |
|---|---|
| 目标目录已存在同名文件 | `"该目录下存在同名文件"` |

---

### 1.3 `POST /file-transfer/upload/direction-connect/whole-file` — 获取整文件上传 URL

> **调用时机**：`upload/auth` 返回 `isUpload: false` 且 `isSlice: false`

#### 请求体

```json
"550e8400-e29b-41d4-a716-446655440000"
```

**注意**：请求体是一个**纯 JSON 字符串**（带双引号），不是 JSON 对象。直接传 `upload/auth` 返回的 `uploadId` 值。

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| 请求体本身 | `String` | ✅ 是 | 上传会话 ID（从上一步获取） |

#### 响应体 — `Result<String>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": "https://minio.example.com/bucket/original/a/b/abc.txt?X-Amz-..."
}
```

`data` 是一个 **预签名 PUT URL**。前端拿到后：

```javascript
// 使用 fetch 上传
const uploadUrl = response.data;  // 预签名 URL
const fileBlob = ...;             // 文件 Blob/File 对象

await fetch(uploadUrl, {
  method: 'PUT',
  body: fileBlob,
  headers: {
    'Content-Type': 'application/octet-stream'
  }
});
```

- URL 有效期 **10 分钟**
- 上传成功后必须调用 `/upload/save` 保存记录

---

### 1.4 `POST /file-transfer/upload/direction-connect/chunk-file` — 获取分片上传 URL

> **调用时机**：`upload/auth` 返回 `isUpload: false` 且 `isSlice: true`

#### 请求体 — `UploadChunkFileDTO`

```json
{
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "chunkNumbers": [1, 2, 3, 4, 5]
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `uploadId` | `String` | ✅ 是 | 上传会话 ID（从 `upload/auth` 获取） |
| `chunkNumbers` | `List<Integer>` | ✅ 是 | 需要上传的分片序号列表，**从 1 开始编号** |

> **断点续传**：如果某些分片上传失败，只传失败的那些分片序号即可重新获取 URL。

#### 响应体 — `Result<Map<Integer, String>>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "1": "https://minio.example.com/bucket/...?partNumber=1&uploadId=xxx&X-Amz-...",
    "2": "https://minio.example.com/bucket/...?partNumber=2&uploadId=xxx&X-Amz-...",
    "3": "https://minio.example.com/bucket/...?partNumber=3&uploadId=xxx&X-Amz-...",
    "4": "https://minio.example.com/bucket/...?partNumber=4&uploadId=xxx&X-Amz-...",
    "5": "https://minio.example.com/bucket/...?partNumber=5&uploadId=xxx&X-Amz-..."
  }
}
```

- Key (`Integer`)：分片序号
- Value (`String`)：该分片的预签名 PUT URL

#### 前端上传分片示例

```javascript
const urlsMap = response.data;  // { 1: "url1", 2: "url2", ... }
const parts = {};               // 收集 ETag，供合并步骤使用

for (const [chunkNum, url] of Object.entries(urlsMap)) {
  const chunk = getFileChunk(chunkNum);  // 获取对应分片的 Blob
  const res = await fetch(url, {
    method: 'PUT',
    body: chunk,
  });
  // 从响应头获取 ETag，去掉首尾双引号
  const etag = res.headers.get('ETag').replace(/^"|"$/g, '');
  parts[parseInt(chunkNum)] = etag;
}
```

- 各分片 URL 可**并发**请求，不必串行
- URL 有效期 **10 分钟**

---

### 1.5 `POST /file-transfer/upload/merge` — 合并分片

> **调用时机**：所有分片上传成功后。**仅分片上传路径需要。**

#### 请求体 — `UploadChunkFileMergeDTO`

```json
{
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "parts": {
    "1": "abc123def456",
    "2": "789ghi012jkl",
    "3": "345mno678pqr"
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `uploadId` | `String` | ✅ 是 | 上传会话 ID |
| `parts` | `Map<Integer, String>` | ✅ 是 | 分片序号 → ETag 的映射。ETag 来自每个分片 PUT 请求的**响应头 `ETag`**（去掉首尾双引号） |

> **注意**：JSON 的 key 只能为 String，传数字 key 会被自动转为字符串。后端会转回 Integer，不影响使用。

#### 响应体 — `Result<Void>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

- 合并成功后，文件信息已自动写入数据库，**无需再调 `/upload/save`**

---

### 1.6 `POST /file-transfer/upload/save` — 保存上传记录

> **调用时机**：整文件上传成功后。**仅整文件上传路径需要。**

#### 请求体

```json
"550e8400-e29b-41d4-a716-446655440000"
```

请求体为**纯 JSON 字符串**，即 `upload/auth` 返回的 `uploadId`。

#### 响应体 — `Result<Void>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

---

## 2. 下载文件

### 2.1 `GET /file-transfer/download/direction-connect/file` — 获取下载 URL

#### 请求参数（Query）

```
GET /file-transfer/download/direction-connect/file?id=42
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `Long` | ✅ 是 | **用户文件 ID**（`user_file` 表主键）。注意：不是你列表里返回的 `fileId`，那个是物理文件 ID。下载用的是用户文件记录的主键 ID。 |

> **如何获取 `id`**：从"我的文件列表"接口返回的每条记录中，取 `id` 字段（不是 `fileId` / `objectId`）。

#### 响应体 — `Result<String>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": "https://minio.example.com/bucket/original/a/b/abc.txt?X-Amz-...&response-content-disposition=attachment..."
}
```

`data` 是一个 **预签名 GET URL**，已附带 `Content-Disposition: attachment`。前端直接访问该 URL 即可触发浏览器下载：

```javascript
const downloadUrl = response.data;
window.open(downloadUrl);  // 或 <a href="..." download>
```

- URL 有效期 **5 分钟**
- 已校验用户权限，仅文件所属用户可下载

#### 异常

| 场景 | 错误信息 |
|---|---|
| 文件不存在或无权限 | `"资源不存在"` |

---

## 3. 预览文件

### 3.1 `GET /file-transfer/preview` — 获取文件预览 URL

#### 请求参数（Query）

```
GET /file-transfer/preview?userFileId=42&fileSize=1048576&contentType=image%2Fpng
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userFileId` | `Long` | ✅ 是 | 用户文件 ID（`user_file` 表主键），从文件列表接口获取 |
| `fileSize` | `Long` | ✅ 是 | 文件大小，单位：字节 |
| `contentType` | `String` | ✅ 是 | 文件 MIME 类型，如 `"image/png"`、`"application/pdf"`。需 URL Encode |

#### 响应体 — `Result<String>`

```json
{
  "code": 200,
  "msg": "ok",
  "data": "https://minio.example.com/bucket/original/a/b/abc.png?X-Amz-...&response-content-disposition=inline..."
}
```

`data` 是一个 **预签名 GET URL**，已附带 `Content-Disposition: inline`。可直接用于：

```html
<!-- 图片 -->
<img src="预签名URL" />

<!-- PDF/视频等 -->
<iframe src="预签名URL" />
<video src="预签名URL" controls />
```

- URL 有效期 **10 分钟**
- 已校验用户权限，仅文件所属用户可预览

#### 异常

| 场景 | 错误信息 |
|---|---|
| 文件不存在或无权限 | `"数据不存在"` |

---

### 3.2 `GET /file-transfer/preview/images/page` — 分页获取图片缩略图列表

> 返回当前用户所有图片文件的分页列表，按创建时间倒序。

#### 请求参数（Query）

```
GET /file-transfer/preview/images/page?pageSize=20&pageNo=1&isAsc=false&sortBy=createTime
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| `pageSize` | `Long` | 否 | `10` | 每页条数 |
| `pageNo` | `Long` | 否 | `1` | 当前页码 |
| `isAsc` | `Boolean` | 否 | `true` | 是否升序 |
| `sortBy` | `String` | 否 | — | 排序字段。默认按创建时间倒序 |

#### 响应体 — `PageResult<PreviewImagesVO>`

```json
{
  "total": 128,
  "pageSize": 10,
  "pageNo": 1,
  "items": [
    {
      "fileId": 3,
      "fileName": "旅游照片.jpg",
      "fileSize": 2097152,
      "thumbUrl": "https://minio.example.com/bucket/original/...?X-Amz-...&response-content-disposition=inline...",
      "createTime": "2026-07-20T15:30:00"
    },
    {
      "fileId": 5,
      "fileName": "头像.png",
      "fileSize": 153600,
      "thumbUrl": "https://minio.example.com/bucket/original/...?X-Amz-...&response-content-disposition=inline...",
      "createTime": "2026-07-19T10:00:00"
    }
  ]
}
```

`items` 中每个元素字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `fileId` | `Long` | 物理文件 ID（`file_object` 表主键） |
| `fileName` | `String` | 文件名 |
| `fileSize` | `Long` | 文件大小（字节） |
| `thumbUrl` | `String` | 缩略图/预览图的预签名 GET URL，可直接作为 `<img src>`。有效期 10 分钟 |
| `createTime` | `String` | 创建时间（ISO 8601 格式） |

---

## 4. 错误码速查

| code | 说明 | 可能出现场景 |
|---|---|---|
| `200` | 成功 | — |
| `10400` | 请求参数异常 | 必填字段缺失、格式错误等 |
| `10500` | 业务异常 | 同名文件冲突、上传任务不存在、数据不存在等 |
| `10601` | 资源不存在 | 下载/预览的文件已被删除 |
| `10700` | 上传文件异常 | 上传过程中出现错误 |
| `20110` | 对象存储服务异常 | MinIO 出现问题，可重试 |

> 错误响应示例：
> ```json
> { "code": 10500, "msg": "该目录下存在同名文件", "data": null }
> ```

---

## 5. 附录：数据结构速查

### 5.1 请求 DTO 汇总

#### `UploadAuthorizationDTO` — 上传授权请求

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `parentId` | `Long` | ✅ | 父目录 ID，根目录为 `0` |
| `fileName` | `String` | ✅ | 文件名（含扩展名） |
| `fileMd5` | `String` | ✅ | 文件 MD5（32 位小写） |
| `fileSize` | `Long` | ✅ | 文件大小（字节） |
| `isSlice` | `Boolean` | ✅ | 是否分片上传 |

#### `UploadChunkFileDTO` — 获取分片 URL 请求

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `uploadId` | `String` | ✅ | 上传会话 ID |
| `chunkNumbers` | `List<Integer>` | ✅ | 分片序号列表（从 1 开始） |

#### `UploadChunkFileMergeDTO` — 分片合并请求

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `uploadId` | `String` | ✅ | 上传会话 ID |
| `parts` | `Map<Integer, String>` | ✅ | 分片序号 → ETag |

#### `PreviewFileDTO` — 预览请求（Query 参数）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userFileId` | `Long` | ✅ | 用户文件 ID |
| `fileSize` | `Long` | ✅ | 文件大小（字节） |
| `contentType` | `String` | ✅ | MIME 类型 |

### 5.2 响应 VO 汇总

#### `UploadFileVO` — 上传授权响应

| 字段 | 类型 | 说明 |
|---|---|---|
| `isUpload` | `Boolean` | `true` = 秒传命中，无需实际上传 |
| `uploadId` | `String` | 上传会话 ID（`isUpload=false` 时有值） |
| `urls` | `Map<Integer, String>` | 分片 URL（`isUpload=false` 时通常为 null） |

#### `PreviewImagesVO` — 图片缩略图列表项

| 字段 | 类型 | 说明 |
|---|---|---|
| `fileId` | `Long` | 物理文件 ID |
| `fileName` | `String` | 文件名 |
| `fileSize` | `Long` | 文件大小（字节） |
| `thumbUrl` | `String` | 缩略图预签名 URL |
| `createTime` | `String` | 创建时间 |

---

## 6. 常见问题

### Q1: 上传文件时，前端如何计算 MD5？

建议使用 `spark-md5` 库的增量计算。对大文件分片计算 MD5 的同时，也可以顺带准备好分片数据。

### Q2: 整文件上传和分片上传的分界线推荐多少？

建议以 **10MB** 为界。小于 10MB 走整文件上传（2 步），大于 10MB 走分片上传（3 步），避免单次 HTTP 上传超时。

### Q3: 分片上传各分片大小推荐多少？

建议每片 **5MB ~ 10MB**。MinIO 要求除最后一片外每片至少 5MB。

### Q4: 分片上传如果某一片失败了怎么办？

对该失败的分片重新调 `/upload/direction-connect/chunk-file`，`chunkNumbers` 只传失败的序号，获取新 URL 后重试。

### Q5: 预签名 URL 过期了怎么办？

重新调用对应的接口获取新 URL。下载和预览的 URL 过期不会影响已上传完成的文件。

### Q6: `upload/auth` 返回的 `uploadId` 能用多久？

Redis 缓存有效期 **10 分钟**。建议前端在 10 分钟内完成上传全流程。如果超时，需要从 `upload/auth` 重新开始。
