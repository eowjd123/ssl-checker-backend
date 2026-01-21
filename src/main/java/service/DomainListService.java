package service;

import dto.DomainListDTO;
import entity.DomainList;
import repository.DomainListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomainListService {

    @Autowired
    private DomainListRepository repository;

    public List<DomainListDTO> getAllLists() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DomainListDTO saveList(DomainListDTO dto) {
        DomainList entity = new DomainList();
        entity.setName(dto.getName());
        entity.setDomains(String.join(",", dto.getDomains()));

        DomainList saved = repository.save(entity);
        return convertToDTO(saved);
    }

    public void deleteList(Long id) {
        repository.deleteById(id);
    }

    private DomainListDTO convertToDTO(DomainList entity) {
        DomainListDTO dto = new DomainListDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDomains(Arrays.asList(entity.getDomains().split(",")));
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}