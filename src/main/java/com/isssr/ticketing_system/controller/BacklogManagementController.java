package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.BacklogItemDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.dto.BacklogItemDto;
import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.BacklogItemNotSavedException;
import com.isssr.ticketing_system.exception.TargetNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BacklogManagementController {

    @Autowired
    private BacklogItemDao backlogItemDao;

    @Autowired
    private TargetDao targetDao;

    /* Il metodo aggiunge un item al product backlog del prodotto con l'id specificato.*/
    public BacklogItemDto addBacklogItem(Long targetId, BacklogItemDto item) throws TargetNotFoundException, BacklogItemNotSavedException {
        Optional<Target> searchedTarget = targetDao.findById(targetId);
        if (!searchedTarget.isPresent()) {
            throw new TargetNotFoundException();
        }
        // La conversione da Dto a Entity viene automatizzata usando la libreria ModelMapper.
        ModelMapper modelMapper = new ModelMapper();
        BacklogItem backlogItem = modelMapper.map(item, BacklogItem.class);
        backlogItem.setProduct(searchedTarget.get());
        BacklogItem addedItem = backlogItemDao.save(backlogItem);
        if (addedItem == null) {
            throw new BacklogItemNotSavedException();
        }
        item.setId(backlogItem.getId());
        return item;
    }
}
