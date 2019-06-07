package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.entrymanager.DomainACLManager;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.enumeration.UserRole;
import com.isssr.ticketing_system.exception.*;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.utils.EntityMergeUtils;
import com.isssr.ticketing_system.utils.PageableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PageableUtils pageableUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityMergeUtils entityMergeUtils;

    @Autowired
    DomainACLManager domainACLManager;

    /*
    @Transactional
    public User create(@NotNull User user) {
        return this.userDao.insertUser(user);
    }
    */

    public List<User> findAllNotCustomer(){
        List<User> userNotCustomer = userDao.findAllNotCustomer();
        return userNotCustomer;
    }

    @Transactional
    @LogOperation(tag = "USER_CREATE", inputArgs = {"user"})
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public User insertUser(@NonNull User user) {

        if (user.getPassword() != null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));

        Optional<User> oldUser = this.userDao.findByUsername(user.getUsername());

        if (oldUser.isPresent())
            user.setId(oldUser.get().getId());

        domainACLManager.createSidForUser(user.getUsername());
        return this.userDao.save(user);

        /*
        User oldUser = null;
        try {
            oldUser = this.findUserByUsername(user.getUsername());
            user.setId(oldUser.getId());
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (user.getPassword() != null)
                user.setPassword(passwordEncoder.encode(user.getPassword()));

            domainACLManager.createSidForUser(user.getUsername());
//user.setId((long) 100);
            return this.userDao.save(user);
        }
        */
    }

    @Transactional
    //@PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.User','READ') or hasAuthority('ROLE_ADMIN')")
    public User findById(@NotNull Long id) throws EntityNotFoundException {
        Optional<User> user = this.userDao.findById(id);

        if (!user.isPresent()) {
            throw new EntityNotFoundException("User not found");
        }

        return user.get();
    }

    @Transactional
    //@PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public User findUserByUsername(@NotNull String username) throws EntityNotFoundException {
        Optional<User> user = this.userDao.findByUsername(username);

        if (!user.isPresent()) {
            throw new EntityNotFoundException("User not found");
        }

        return user.get();
    }

    @Transactional
    //@PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.User','READ') or hasAuthority('ROLE_ADMIN')")
    public boolean existsById(@NotNull Long id) {
        return this.userDao.existsById(id);
    }

    /*
    @Transactional
    public Iterable<User> findAll() {
        return this.userDao.findAll();
    }

    @Transactional
    public Iterable<User> findAllById(@NotNull Iterable<Long> ids) {
        return this.userDao.findAllById(ids);
    }
*/
    @Transactional
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public long count() {
        return this.userDao.count();
    }

    @Transactional
    //@LogOperation(tag = "USER_DELETE", inputArgs = {"id"})
    //@PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.User','DELETE') or hasAuthority('ROLE_ADMIN')")
    public boolean deleteById(@NotNull Long id) throws NotFoundEntityException {
        Optional<User> user = this.userDao.findById(id);
        if (!user.isPresent())
            throw new NotFoundEntityException();

        user.get().delete();
        this.userDao.save(user.get());

        //domainACLManager.deleteSidForUser(user.getUsername());
        return true;
    }

    @Transactional
    @LogOperation(tag = "USER_UPDATE", inputArgs = {"user"})
    //@PreAuthorize("#user.username == principal.username or hasAuthority('ROLE_ADMIN')")
    public User updateById(@NotNull Long id, @NonNull User user) throws EntityNotFoundException {
        if (!userDao.existsById(id))
            throw new EntityNotFoundException("User to update not found in DB, maybe you have to create a new one");

        user.setId(id);

        if (user.getPassword() != null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userDao.save(entityMergeUtils.merge(user));
    }

    @Transactional
    @LogOperation(tag = "USER_UPDATE", inputArgs = {"user"})
    //@PreAuthorize("#user.username == principal.username or hasAuthority('ROLE_ADMIN')")
    public User updateByUsername(@NotNull String username, @NotNull User user) throws EntityNotFoundException {

        Optional<User> foundUser = userDao.findByUsername(username);

        if (!foundUser.isPresent())
            throw new EntityNotFoundException("User to update not found in DB, maybe you have to create a new one");

        user.setId(foundUser.get().getId());

        if (user.getPassword() != null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userDao.save(entityMergeUtils.merge(user));
    }

    @Transactional
    //@LogOperation(tag = "USER_DELETE")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteAll() {
        this.userDao.deleteAll();
    }

    @Transactional
    public boolean existsByEmail(@NotNull String email) {
        return this.userDao.existsByEmail(email);
    }


    @Transactional
    public boolean existsByUsername(@NotNull String username) {
        return this.userDao.existsByUsername(username);
    }

    @Transactional
    public Optional<User> findByEmail(@NotNull String email) {
        return this.userDao.findByEmail(email);
    }

    @Transactional
    @LogOperation(tag = "USER_UPDATE", inputArgs = {"user"})
    //@PreAuthorize("hasPermission(#user.id, 'UPDATE') or hasAuthority('ROLE_ADMIN')")
    public User updateByEmail(@NotNull String email, @NotNull User user) throws EntityNotFoundException {

        Optional<User> foundUser = userDao.findByEmail(email);

        if (!foundUser.isPresent())
            throw new EntityNotFoundException("User to update not found in DB, maybe you have to create a new one");

        user.setId(foundUser.get().getId());

        if (user.getPassword() != null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userDao.save(entityMergeUtils.merge(user));
    }


    @Transactional
    //@PostFilter("hasPermission(filterObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return this.userDao.findAll();
    }


    @Transactional
    //@PostFilter("hasPermission(filterObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public List<User> findByIdIn(@NotNull List<Long> ids) {
        return userDao.findByIdIn(ids);
    }

    @Transactional
    //@PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public User getUser(@NotNull Long id) {

        Optional<User> user = userDao.findById(id);
        if (!user.isPresent()) {
            throw new DomainEntityNotFoundException(id, User.class);
        }
        return user.get();
        //return userDAO.getOne(ID);
    }


    public User getTeamCoordinator()
    {
        List<User> teamCoordinators = userDao.getTeamCoordinators();
        if(teamCoordinators.size()==1)
            return teamCoordinators.get(0);
        int selectedTeamCoordinator = (int)(Math.random()*teamCoordinators.size());
        return teamCoordinators.get(selectedTeamCoordinator);
    }


    public List<? extends User> getEmployedUserByRole(UserRole role) {
        switch (role)
        {
            case TEAM_LEADER:
                return getListEmployedTeamLeader();
            case TEAM_MEMBER:
                return getListEmployedTeamMember();
            case TEAM_COORDINATOR:
                return getListTeamCoordinator();
        }
        return null;
    }


    public List<User> getListEmployedTeamLeader() {
        return userDao.getListEmployedTeamLeader();

    }

    private List<? extends User> getListEmployedTeamMember() {
        return userDao.getListEmployedTeamMember();

    }

    private List<User> getListTeamCoordinator()
    {
        return userDao.getTeamCoordinators();
    }


    public Long getMaxId() {

        return userDao.getMaxId();
    }
}
