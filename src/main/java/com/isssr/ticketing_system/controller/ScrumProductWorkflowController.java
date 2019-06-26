package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumProductWorkflowDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.dto.ScrumProductWorkflowDto;
import com.isssr.ticketing_system.entity.ScrumProductWorkflow;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.exception.ScrumProductWorkflowNotSavedException;
import com.isssr.ticketing_system.exception.UpdateException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScrumProductWorkflowController {

    @Autowired
    ScrumProductWorkflowDao scrumProductWorkflowDao;
    @Autowired
    TargetDao targetDao;

    public ScrumProductWorkflowDto addScrumProductWorkflow(ScrumProductWorkflowDto workflowDto)
            throws ScrumProductWorkflowNotSavedException {

        ModelMapper modelMapper = new ModelMapper();
        ScrumProductWorkflow workflow = modelMapper.map(workflowDto, ScrumProductWorkflow.class);
        ScrumProductWorkflow addedWorkflow = scrumProductWorkflowDao.save(workflow);
        if (addedWorkflow == null) {
            throw new ScrumProductWorkflowNotSavedException();
        }
        return modelMapper.map(addedWorkflow, ScrumProductWorkflowDto.class);

    }

    public List<ScrumProductWorkflowDto> getAllScrumProductWorkflow() {
        ModelMapper modelMapper = new ModelMapper();
        List<ScrumProductWorkflowDto> scrumProductWorkflowDtos = new ArrayList<>();
        List<ScrumProductWorkflow> scrumProductWorkflows = scrumProductWorkflowDao.findAll();
        for (ScrumProductWorkflow scrumProductWorkflow : scrumProductWorkflows){
            ScrumProductWorkflowDto scrumProductWorkflowDto =
                    modelMapper.map(scrumProductWorkflow, ScrumProductWorkflowDto.class);
            scrumProductWorkflowDtos.add(scrumProductWorkflowDto);
        }
        return scrumProductWorkflowDtos;
    }

    public void removeScrumProductWorkflow(Long scrumProductWorkflowId) throws NotFoundEntityException, UpdateException {
        Optional<ScrumProductWorkflow> scrumProductWorkflow = scrumProductWorkflowDao.findById(scrumProductWorkflowId);
        if (!scrumProductWorkflow.isPresent()){
            throw new NotFoundEntityException();
        }

        // Se esiste uno prodotto con il workflow selezionato l'eliminazione viene impedita
        List<Target> targetWithSelectedWorkflow =  targetDao.findAllByScrumProductWorkflow(scrumProductWorkflow.get());
        if (!targetWithSelectedWorkflow.isEmpty()){
            throw new UpdateException();
        }

        scrumProductWorkflowDao.delete(scrumProductWorkflow.get());
    }

    public ScrumProductWorkflowDto updateScrumProductWorkflow(ScrumProductWorkflowDto scrumProductWorkflowDto)
            throws NotFoundEntityException, UpdateException {

        Optional<ScrumProductWorkflow> scrumProductWorkflow = scrumProductWorkflowDao.findById(scrumProductWorkflowDto.getId());
        if (!scrumProductWorkflow.isPresent()){
            throw new NotFoundEntityException();
        }

        // Se esiste uno prodotto con il workflow selezionato avente uno sprint attivo, l'operazione di modifica viene
        // impedita e un opportuna eccezione viene sollevata
        List<Target> targetWithSelectedWorkflow =  targetDao.findAllByScrumProductWorkflow(scrumProductWorkflow.get());
        for (Target target : targetWithSelectedWorkflow){
            for(Sprint sprint : target.getSprints()){
                if (sprint.getIsActive() != null && sprint.getIsActive()){
                    throw new UpdateException();
                }
            }
        }

        // Altrimenti viene effettuata la modifica
        scrumProductWorkflow.get().setName(scrumProductWorkflowDto.getName());
        scrumProductWorkflow.get().setStates(scrumProductWorkflowDto.getStates());
        scrumProductWorkflowDao.save(scrumProductWorkflow.get());
        return scrumProductWorkflowDto;
    }

}
