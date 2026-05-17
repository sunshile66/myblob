package com.myblob.module.filemanager.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.filemanager.service.FileManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/filemanager")
@RequiredArgsConstructor
public class FileManagerController {

    private final FileManagerService fileManagerService;

    @GetMapping("/files/")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getMyFiles(
            @RequestParam(required = false) Long folder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(fileManagerService.getMyFiles(folder, page, size)));
    }

    @PostMapping("/files/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder_id", required = false) Long folderId) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileManagerService.uploadFile(file, folderId)));
    }

    @DeleteMapping("/files/{id}/")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileManagerService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("文件删除成功", null));
    }

    @GetMapping("/folders/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyFolders(
            @RequestParam(required = false) Long parent) {
        return ResponseEntity.ok(ApiResponse.success(fileManagerService.getMyFolders(parent)));
    }

    @PostMapping("/folders/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFolder(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long parentId = body.get("parent_id") != null
                ? Long.valueOf(body.get("parent_id").toString()) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileManagerService.createFolder(name, parentId)));
    }

    @DeleteMapping("/folders/{id}/")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable Long id) {
        fileManagerService.deleteFolder(id);
        return ResponseEntity.ok(ApiResponse.success("文件夹删除成功", null));
    }
}
