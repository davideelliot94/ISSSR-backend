package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.BacklogItemDao;
import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.BacklogItemDto;
import com.isssr.ticketing_system.dto.TargetDto;
import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.BacklogItemNotSavedException;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.TargetNotFoundException;
import com.isssr.ticketing_system.exception.UsernameNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BacklogManagementController {

    @Autowired
    private BacklogItemDao backlogItemDao;

    @Autowired
    private TargetDao targetDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ScrumTeamDao scrumTeamDao;

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

    /* Il metodo restituisce l'elenco dei prodotti sui quali sta lavorando almeno uno Scrum Team a cui afferisce
    l'utente con username specificato.
     */
    public List<TargetDto> findProductByScrumUser(String username) throws EntityNotFoundException {
        // Si ottiene l'utente con l'username specificato
        Optional<User> user = userDao.findByUsername(username);
        if (!user.isPresent()) {
            // Se non esiste un utente con l'username specificato viene sollevata un eccezione
            throw new EntityNotFoundException();
        }

        List<Target> products = new ArrayList<>();

        // Si individuano tutti gli ScrumTeam di cui l'utente è Scrum Master
        List<ScrumTeam> scrumTeamWithUserAsScrumMaster = scrumTeamDao.findAllByScrumMaster(user.get());
        // Si inseriscono tutti i prodotti sui quali lavora lo Scrum Team tra quelli da restituire
        for (ScrumTeam team : scrumTeamWithUserAsScrumMaster){
            for (Target product : team.getProducts()){
                products.add(product);
            }
        }

        // Si individuano tutti gli ScrumTeam di cui l'utente è Product Owner
        List<ScrumTeam> scrumTeamWithUserAsProductOwner = scrumTeamDao.findAllByProductOwner(user.get());
        // Si inseriscono tutti i prodotti sui quali lavora lo Scrum Team tra quelli da restituire
        for (ScrumTeam team : scrumTeamWithUserAsProductOwner){
            for (Target product : team.getProducts()){
                products.add(product);
            }
        }

        // Si individuano tutti gli ScrumTeam di cui l'utente è Team Member
        List<ScrumTeam> scrumTeamWithUserAsTeamMember = scrumTeamDao.findAllByTeamMembersContains(user.get());
        // Si inseriscono tutti i prodotti sui quali lavora lo Scrum Team tra quelli da restituire
        for (ScrumTeam team : scrumTeamWithUserAsTeamMember){
            for (Target product : team.getProducts()){
                products.add(product);
            }
        }

        // Si convertono tutti i prodotti in TargetDto e si restituiscono
        List<TargetDto> targetDtos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        for (Target product : products){
            TargetDto targetDto = modelMapper.map(product, TargetDto.class);
            targetDtos.add(targetDto);
        }

        return targetDtos;
    }
}
