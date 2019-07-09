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

    /* Inserisce nello strato di persistenza un worflow Scrum per un prodotto*/
    public ScrumProductWorkflowDto addScrumProductWorkflow(ScrumProductWorkflowDto workflowDto)
            throws ScrumProductWorkflowNotSavedException {

        ModelMapper modelMapper = new ModelMapper();
        // conversione Dto->Entity
        ScrumProductWorkflow workflow = modelMapper.map(workflowDto, ScrumProductWorkflow.class);
        // salvataggio entity
        ScrumProductWorkflow addedWorkflow = scrumProductWorkflowDao.save(workflow);
        if (addedWorkflow == null) {
            throw new ScrumProductWorkflowNotSavedException();
        }
        return modelMapper.map(addedWorkflow, ScrumProductWorkflowDto.class);

    }

    /* Restituisce la lista di tutti i Product Workflow Scrum presenti nel layer di persistenza*/
    public List<ScrumProductWorkflowDto> getAllScrumProductWorkflow() {
        ModelMapper modelMapper = new ModelMapper();
        List<ScrumProductWorkflowDto> scrumProductWorkflowDtos = new ArrayList<>();
        List<ScrumProductWorkflow> scrumProductWorkflows = scrumProductWorkflowDao.findAll();
        // conversione Entity->Dto
        for (ScrumProductWorkflow scrumProductWorkflow : scrumProductWorkflows){
            ScrumProductWorkflowDto scrumProductWorkflowDto =
                    modelMapper.map(scrumProductWorkflow, ScrumProductWorkflowDto.class);
            scrumProductWorkflowDtos.add(scrumProductWorkflowDto);
        }
        return scrumProductWorkflowDtos;
    }

    /* Cancella il Product Workflow Scrum avente l'id specificato, assicurandosi che non ci siano prodotti ad esso associati*/
    public void removeScrumProductWorkflow(Long scrumProductWorkflowId) throws NotFoundEntityException, UpdateException {
        // si verifica l'esistenza di un workflow con l'id specificato
        Optional<ScrumProductWorkflow> scrumProductWorkflow = scrumProductWorkflowDao.findById(scrumProductWorkflowId);
        if (!scrumProductWorkflow.isPresent()){
            throw new NotFoundEntityException();
        }
        // Se esiste prodotto a cui è associato il workflow selezionato, l'eliminazione viene impedita
        List<Target> targetWithSelectedWorkflow =  targetDao.findAllByScrumProductWorkflow(scrumProductWorkflow.get());
        if (!targetWithSelectedWorkflow.isEmpty()){
            throw new UpdateException();
        }

        scrumProductWorkflowDao.delete(scrumProductWorkflow.get());
    }

    /*
    * Aggiorna un Product Workflow Scrum con le informazioni contenute all'interno del Dto dato, assicurandosi che non
    * esistano Sprint attivi che coinvolgono un prodotto a cui è stato assegnato il workflow oggetto della modifica.
    * */
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
