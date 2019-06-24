package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumCeremonyActivityDao;
import com.isssr.ticketing_system.dao.ScrumCeremonyDao;
import com.isssr.ticketing_system.dao.SprintDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.ScrumCeremonyActivityDto;
import com.isssr.ticketing_system.dto.ScrumCeremonyDto;
import com.isssr.ticketing_system.dto.UserDto;
import com.isssr.ticketing_system.entity.ScrumCeremony;
import com.isssr.ticketing_system.entity.ScrumCeremonyActivity;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.enumeration.ScrumCeremonyType;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.ScrumCeremonyNotSavedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScrumCeremonyController {

    @Autowired
    private ScrumCeremonyDao scrumCeremonyDao;

    @Autowired
    private SprintDao sprintDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ScrumCeremonyActivityDao scrumCeremonyActivityDao;

    /* Il metodo salva nel database una scrum ceremony*/
    @Transactional
    public ScrumCeremonyDto addScrumCeremony(ScrumCeremonyDto scrumCeremonyDto) throws ScrumCeremonyNotSavedException, EntityNotFoundException {


        // Il DTO viene mappato sulla entity corrispondente
        ScrumCeremony scrumCeremony = new ScrumCeremony();
        scrumCeremony.setType(ScrumCeremonyType.valueOf(scrumCeremonyDto.getType()));
        scrumCeremony.setDate(scrumCeremonyDto.getDate());
        scrumCeremony.setDuration(scrumCeremonyDto.getDuration());

        // La scrum ceremony è asssociata ad uno sprint. Se uno sprint con l'id specificato non esiste, allora viene
        // sollevata un'eccezione
        Long sprintId = scrumCeremonyDto.getSprintId();
        Optional<Sprint> sprintSearchResult = sprintDao.findById(sprintId);
        if (!sprintSearchResult.isPresent()) {
            throw new EntityNotFoundException();
        }
        scrumCeremony.setSprint(sprintSearchResult.get());

        // La scrum ceremony ha associati dei partecipanti. Se uno dei partecipanti indicati non esiste, viene
        // sollevata un'eccezione
        List<User> scrumCeremonyParticipants = new ArrayList<>();

        for (UserDto participant: scrumCeremonyDto.getParticipants()) {
            Optional<User> userSearchResult = userDao.findById(participant.getId());
            if (!userSearchResult.isPresent()) {
                throw new EntityNotFoundException();
            }
            scrumCeremonyParticipants.add(userSearchResult.get());

        }
        scrumCeremony.setParticipants(scrumCeremonyParticipants);

        // Alla Scrum Ceremony è associato un insieme di attività.
        List<ScrumCeremonyActivity> activities = new ArrayList<>();
        for (ScrumCeremonyActivityDto activityDto : scrumCeremonyDto.getActivities()) {
            ScrumCeremonyActivity scrumCeremonyActivity = new ScrumCeremonyActivity();
            scrumCeremonyActivity.setName(activityDto.getName());
            scrumCeremonyActivity.setComment(activityDto.getComment());
            activities.add(scrumCeremonyActivity);
        }

        // Salvataggio scrum ceremony
        scrumCeremony.setActivities(activities);
        ScrumCeremony addedScrumCeremony = scrumCeremonyDao.save(scrumCeremony);
        if (addedScrumCeremony == null) {
            throw new ScrumCeremonyNotSavedException();
        }

        // Salvataggio attività della scrum ceremony
        for (ScrumCeremonyActivity activity : activities) {
            activity.setScrumCeremony(addedScrumCeremony);
            ScrumCeremonyActivity savedActivity = scrumCeremonyActivityDao.save(activity);
            if (savedActivity == null) {
                throw new ScrumCeremonyNotSavedException();
            }
        }

        scrumCeremonyDto.setId(addedScrumCeremony.getId());
        List<UserDto> participants = new ArrayList<>();
        for (User user : scrumCeremonyParticipants) {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            participants.add(userDto);
        }
        scrumCeremonyDto.setParticipants(participants);
        return scrumCeremonyDto;

    }

    // Restituisce le Scrum Ceremonies associate allo Sprint avente l'id specificato
    public List<ScrumCeremonyDto> findScrumCeremoniesBySprint(Long sprintId) throws EntityNotFoundException {
        Optional<Sprint> sprintSearchResult = sprintDao.findById(sprintId);
        if (!sprintSearchResult.isPresent()) {
            throw new EntityNotFoundException();
        }
        List<ScrumCeremony> scrumCeremonies = scrumCeremonyDao.findBySprint(sprintSearchResult.get());
        List<ScrumCeremonyDto> scrumCeremonyDtos = new ArrayList<>();
        if (scrumCeremonies == null) {
            return scrumCeremonyDtos;
        }
        for (ScrumCeremony ceremony : scrumCeremonies) {
            ScrumCeremonyDto scrumCeremonyDto = new ScrumCeremonyDto();
            scrumCeremonyDto.setId(ceremony.getId());
            scrumCeremonyDto.setType(String.valueOf(ceremony.getType()));
            scrumCeremonyDto.setDate(ceremony.getDate());
            scrumCeremonyDto.setDuration(ceremony.getDuration());
            scrumCeremonyDto.setSprintId(ceremony.getSprint().getId());
            List<UserDto> participants = new ArrayList<>();
            for (User participant : ceremony.getParticipants()) {
                UserDto userDto = new UserDto();
                userDto.setId(participant.getId());
                userDto.setFirstName(participant.getFirstName());
                userDto.setLastName(participant.getLastName());
                participants.add(userDto);
            }

            scrumCeremonyDto.setParticipants(participants);
            List<ScrumCeremonyActivityDto> activities = new ArrayList<>();
            for (ScrumCeremonyActivity activity : ceremony.getActivities()) {
                ScrumCeremonyActivityDto activityDto = new ScrumCeremonyActivityDto();
                activityDto.setId(activity.getId());
                activityDto.setComment(activity.getComment());
                activityDto.setName(activity.getName());
                activities.add(activityDto);
            }
            scrumCeremonyDto.setActivities(activities);
            scrumCeremonyDtos.add(scrumCeremonyDto);
        }
        return scrumCeremonyDtos;
    }

}
