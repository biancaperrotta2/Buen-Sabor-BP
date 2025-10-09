package com.aluracursos.buensaborbp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<Request, BaseResponse, FullResponse, ID> {

        // Estos métodos los implementará cada service concreto que herede de BaseServiceImpl
        protected abstract List<BaseResponse> getAll();
        protected abstract FullResponse getById(ID id);
        protected abstract FullResponse create(Request request);
        protected abstract FullResponse update(ID id, Request request);
        protected abstract void delete(ID id);

        @GetMapping("/all")
        public ResponseEntity<List<BaseResponse>> findAll() {
            return ResponseEntity.ok(getAll());
        }

        @GetMapping("/{id}")
        public ResponseEntity<FullResponse> findById(@PathVariable ID id) {
            return ResponseEntity.ok(getById(id));
        }

        @PostMapping
        public ResponseEntity<FullResponse> createEntity(@RequestBody Request request) {
            return ResponseEntity.ok(create(request));
        }

        @PutMapping("/{id}")
        public ResponseEntity<FullResponse> updateEntity(@PathVariable ID id, @RequestBody Request request) {
            return ResponseEntity.ok(update(id, request));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteEntity(@PathVariable ID id) {
            delete(id);
            return ResponseEntity.noContent().build();
        }
}
