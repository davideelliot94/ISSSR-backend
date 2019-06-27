package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.defaultpermission.TargetDefaultPermission;
import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.SprintDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.ScrumAssignmentDto;
import com.isssr.ticketing_system.dto.ScrumProductWorkflowDto;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
import com.isssr.ticketing_system.dto.TargetDto;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.enumeration.TargetState;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.dao.TargetDao;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Questa classe definisce metodi che si interfacciano con la classe TargetDao per l'interazione con il DB.
 */
@Service
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class TargetController {

    private TargetDao targetDao;
    private TargetDefaultPermission defaultPermissionTable;
    private StateMachineController stateMachineController;
    @Autowired
    private ScrumTeamDao scrumTeamDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private SprintDao sprintDao;



    @Autowired
    public TargetController(TargetDao targetDao, TargetDefaultPermission defaultPermissionTable, StateMachineController stateMachineController) {
        this.targetDao = targetDao;
        this.defaultPermissionTable = defaultPermissionTable;
        this.stateMachineController = stateMachineController;
    }

    /**
     * Metodo usato per inserire un target nel DB.
     *
     * @param target team che va aggiunto al DB.
     * @return info del target aggiunto al DB
     */
    @Transactional
    @LogOperation(tag = "TARGET_CREATE", inputArgs = {"target"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SOFTWARE_PRODUCT_COORDINATOR')")
    public @NotNull Target insertTarget(@NotNull Target target) {
        Target newTarget = this.targetDao.save(target);
        defaultPermissionTable.grantDefaultPermission(newTarget.getId());
        defaultPermissionTable.denyDefaultPermission(newTarget.getId());

        return newTarget;
    }

    /**
     * Metodo usato per aggiornare il target specificato
     *
     * @param id id del target che deve essere aggiornato
     * @param updatedTarget target con i campi aggiornati
     * @return target aggiornato
     */
    @Transactional
    @LogOperation(tag = "TARGET_UPDATE", inputArgs = {"target"})
    @PreAuthorize("hasPermission(#updatedTarget,'WRITE') or hasAuthority('ROLE_ADMIN')")
    public @NotNull Target updateTargetById(@NotNull Long id, @NotNull Target updatedTarget) throws NotFoundEntityException {
        Optional<Target> toBeUpdatedTarget = targetDao.findById(id);
        if (!toBeUpdatedTarget.isPresent())
            throw new NotFoundEntityException();
        toBeUpdatedTarget.get().updateTarget(updatedTarget);
        return targetDao.save(toBeUpdatedTarget.get());
    }

      /**
     * Restituisce il target che ha l'id specificato
     *
     * @param targetId id del target richiesto
     * @return target cercato
     */
    @Transactional
    @PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public Target getTargetById(Long targetId) throws NotFoundEntityException {
        Optional<Target> target = targetDao.findById(targetId) ;
        if (!target.isPresent())
            throw new NotFoundEntityException();
        return target.get();
    }


    /**
     * Restituisce tutti i target associati al Product Owner non aventi Sprint attivi
     * @param productOwnerId id dell'utente che fa da Product Owner
     * @return targets cercati
     */
    @Transactional
//  @PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')") //TODO hasAutority PRODUCT OWNER
     public List<TargetDto> getTargetByProductOwnerIdWithNotActiveSprint(Long productOwnerId) throws NotFoundEntityException {

        User productOwner = userDao.findById(productOwnerId).get();
        List<ScrumTeam> scrumTeams = scrumTeamDao.findAllByProductOwner(productOwner);
        List<Target> targets = targetDao.findAllByScrumTeamIn(scrumTeams);
        ModelMapper modelMapper = new ModelMapper();
        List<TargetDto> targetDtos = new ArrayList<>();
        for (Target target : targets){
            if (target.getSprints().size() == 0){
                TargetDto targetDto = modelMapper.map(target, TargetDto.class);
                targetDtos.add(targetDto);
            }
        }
        return targetDtos;
    }

    @Transactional
//  @PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')") //TODO hasAutority PRODUCT OWNER
    public List<TargetDto> getTargetByProductOwnerId(Long productOwnerId) throws NotFoundEntityException {

        User productOwner = userDao.findById(productOwnerId).get();
        List<ScrumTeam> scrumTeams = scrumTeamDao.findAllByProductOwner(productOwner);
        List<Target> targets = targetDao.findAllByScrumTeamIn(scrumTeams);
        ModelMapper modelMapper = new ModelMapper();
        List<TargetDto> targetDtos = new ArrayList<>();
        for (Target target : targets){
            TargetDto targetDto = modelMapper.map(target, TargetDto.class);
            targetDtos.add(targetDto);
        }
        return targetDtos;
    }

    /**
     * Verifica se il target che ha l'id specificato è presente nel DB.
     *
     * @param id id del target su cui effettuare il controllo
     * @return true se il target cercato esiste, false altrimenti
     */
    @Transactional
    public boolean existsById(Long id) {
        return this.targetDao.existsById(id);
    }

    /**
     * Restituisce il numero di target presenti nel DB.
     *
     * @return numero di target presenti nel DB.
     */
    @Transactional
    public long count() {
        return this.targetDao.count();
    }

    /**
     * Elimina il target che ha l'id specificato
     *
     * @param id id del target da eliminare
     * @return true se il target è stato cancellato, false altrimenti
     */
    @Transactional
    @PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.SoftwareProduct','DELETE') or hasAuthority('ROLE_ADMIN')")
    public boolean deleteTargetById(Long id) throws NotFoundEntityException {
        Optional<Target> target = this.targetDao.findById(id);
        if (!target.isPresent())
            throw new NotFoundEntityException();

        target.get().delete();
        this.targetDao.save(target.get());
        defaultPermissionTable.removeDefaultPermission(id);

        return true;
    }

    /**
     * Elimina tutti i target presenti nel DB.
     *
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteAll() {
        this.targetDao.deleteAll();
    }

    /**
     * Verifica se il target che ha il nome specificato esiste nel DB.
     *
     * @param name nome del target cercato
     * @return true se il target cercato esiste, false altrimenti
     */
    @Transactional
    public boolean existsByName(String name) {
        return this.targetDao.existsByName(name);
    }


    /**
     * Restituisce il target che ha il nome specificato.
     *
     * @param name nome del target cercato
     * @return target cercato
     */
    @Transactional
    @PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public Target getByName(String name) throws EntityNotFoundException {
        Target target = this.targetDao.findByName(name);

        if (target == null) {
            throw new EntityNotFoundException("TARGET_NOT_FOUND");
        }

        return target;
    }


    /**
     * Restituisce tutti i target presenti nel sistema.
     *
     * @return lista dei target presenti nel sistema.
     */
    @Transactional
    //@PostFilter("hasPermission(filterObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public List<Target> getAllTargets() {
        return targetDao.findAll();
    }


    /**
     * Cambia lo stato del target con id specificato
     *
     * @param id del target da aggiornare
     * @param targetState nuovo stato
     * @return target aggiornato
     * @throws NotFoundEntityException
     */
    @Transactional
    public Target changeStateTarget(@NotNull Long id, TargetState targetState) throws NotFoundEntityException {
        Optional<Target> toBeUpdatedTarget = targetDao.findById(id);
        if (!toBeUpdatedTarget.isPresent())
            throw new NotFoundEntityException();

        toBeUpdatedTarget.get().setTargetState(targetState);
        return targetDao.save(toBeUpdatedTarget.get());
    }

    /**
     * Restituisce i target attivi presenti nel DB.
     *
     * @return lista di target attivi
     */
    @Transactional
    @PostFilter("hasPermission(filterObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public List<Target> getActiveTargets() {
        return targetDao.getActiveTarget(TargetState.ACTIVE);
    }

    /**
     * Restituisce gli stati attuali per un target specificato
     *
     * @param targetID id del targt
     * @param role ruolo
     * @return lista di stati
     * @throws NotFoundEntityException
     */
    public List<String> getActualStates(Long targetID, String role) throws NotFoundEntityException {
        Target target = getTargetById(targetID);
        String stateMachineName = target.getStateMachineName();
        return stateMachineController.getActualStates(stateMachineName,role);

    }

    /**
     * Restituisce il next state per un target dato lo stato corrente e l'id
     *
     * @param targetID id del target
     * @param currentState stato corrente
     * @return lista dei nuovi stati
     * @throws NotFoundEntityException
     */
    public ArrayList<ArrayList<String>> getNextStates(Long targetID, String currentState) throws NotFoundEntityException {
        Target target = getTargetById(targetID);
        String stateMachineName = target.getStateMachineName();
        return stateMachineController.getNextStates(stateMachineName,currentState);

    }

    // Restituisce gli assegnamenti dei prodotti agli scrum team
    public List<ScrumAssignmentDto> getScrumAssignments() {
        List<Target> assignedTargets = targetDao.findAllByScrumProductWorkflowIsNotNullAndScrumTeamIsNotNull();
        List<ScrumAssignmentDto> assignments = new ArrayList<>();
        for (Target assigned : assignedTargets) {
            ScrumAssignmentDto assignment = new ScrumAssignmentDto();
            assignment.setProduct(assigned.getName());
            assignment.setScrumProductWorkflow(assigned.getScrumProductWorkflow().getName());
            assignment.setScrumTeam(assigned.getScrumTeam().getName());
            assignments.add(assignment);
        }
        return assignments;
    }
}
