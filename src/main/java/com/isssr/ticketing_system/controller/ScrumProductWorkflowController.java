package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumProductWorkflowDao;
import com.isssr.ticketing_system.dto.ScrumProductWorkflowDto;
import com.isssr.ticketing_system.entity.ScrumProductWorkflow;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.exception.ScrumProductWorkflowNotSavedException;
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

    public void removeScrumProductWorkflow(Long scrumProductWorkflowId) throws NotFoundEntityException {
        Optional<ScrumProductWorkflow> scrumProductWorkflow = scrumProductWorkflowDao.findById(scrumProductWorkflowId);
        if (!scrumProductWorkflow.isPresent()){
            throw new NotFoundEntityException();
        }
        scrumProductWorkflowDao.delete(scrumProductWorkflow.get());
    }

    public ScrumProductWorkflowDto updateScrumProductWorkflow(ScrumProductWorkflowDto scrumProductWorkflowDto)
            throws NotFoundEntityException {
        Optional<ScrumProductWorkflow> scrumProductWorkflow = scrumProductWorkflowDao.findById(scrumProductWorkflowDto.getId());
        if (!scrumProductWorkflow.isPresent()){
            throw new NotFoundEntityException();
        }
        scrumProductWorkflow.get().setName(scrumProductWorkflowDto.getName());
        scrumProductWorkflow.get().setStates(scrumProductWorkflowDto.getStates());
        scrumProductWorkflowDao.save(scrumProductWorkflow.get());
        return scrumProductWorkflowDto;
    }
}
