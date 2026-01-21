package controller;

import dto.DomainListDTO;
import service.DomainListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domain-lists")
@CrossOrigin(origins = "*")
public class DomainListController {

    @Autowired
    private DomainListService service;

    @GetMapping
    public List<DomainListDTO> getAllLists() {
        return service.getAllLists();
    }

    @PostMapping
    public DomainListDTO saveList(@RequestBody DomainListDTO dto) {
        return service.saveList(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteList(@PathVariable Long id) {
        service.deleteList(id);
    }
}