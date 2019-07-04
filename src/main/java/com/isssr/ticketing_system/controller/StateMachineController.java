package com.isssr.ticketing_system.controller;


import Action.FSMAction;
import FSM.FSM;
import States.FSMState;
import com.isssr.ticketing_system.dao.StateMachineDao;
import com.isssr.ticketing_system.entity.StateMachine;
import com.isssr.ticketing_system.enumeration.TicketStatus;
import com.isssr.ticketing_system.enumeration.UserRole;
import com.isssr.ticketing_system.utils.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StateMachineController {

    private final StateMachineDao stateMachineDao;

    @Autowired
    public StateMachineController(StateMachineDao stateMachineDao) {
        this.stateMachineDao = stateMachineDao;
    }

    public Collection<StateMachine> getStateMachines(){
        return stateMachineDao.findAll();
    }

    @Transactional
    public String saveStateMachine(StateMachine stateMachine){

        if(stateMachine.getBase64StateMachine() == null)
            return "MISSING XML FILE";

        String savedStateMachine = stateMachine.getBase64StateMachine();
        ClassPathResource classPathResource = new ClassPathResource("/state_machine/xml_files/");
        String relativePath = null;
        try {
            relativePath = classPathResource.getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Creazione del File XML della FSM:
        FileManager.convertStringToFile(savedStateMachine, stateMachine.getName(), relativePath);

        // Se la macchina a stati inserita non è valida restituisco una Stringa di errore, altrimenti null.
        File file = new File(relativePath, stateMachine.getName());
        String result = stateMachineValidation(file.getPath() + ".xml");

        if(result!=null) {
            Path path = Paths.get(file.getPath() + ".xml");
            try {
                Files.delete(path); // Cancellazione del file XML di una FSM non valida.
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        try{
            stateMachineDao.save(stateMachine); // Salvataggio della FSM nel DB.
        }
        catch (Exception e){
            return "ERROR SAVING STATE MACHINE";
        }

        return null; // Il file XML della FSM è valido.
    }

    /**
     * Verifica che la macchina a stati rispetti i vincoli
     * <ul>
     *     <li>Tutti i ruoli facciano parte dell'insieme dei ruoli del sistema</li>
     *     <li>Tutti gli stati facciano parte dell'insieme degli stati del sistema</li>
     *     <li>Il primo stato sia Dispatching/Validation</li>
     *     <li>Sia presente lo stato EXECUTION</li>
     *     <li>L'ultimo stato sia CLOSED</li>
     *     <li>La macchina a stati sia connessa</li>
     * </ul>
     *
     * @param SMPath path in cui è salvata la macchina a stati.
     * @return una stringa con l'errore oppure null
     */
    @SuppressWarnings("unchecked")
    private String stateMachineValidation(String SMPath){
        FSM stateMachine ;
        try {
            stateMachine = new FSM(SMPath, new FSMAction() {
                @Override
                public boolean action(String curState, String message, String nextState, Object args) {
                    System.out.println(curState + ":" + message + " : " + nextState);
                    return true;
                }
            });
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();

            return "INVALID XML";
        }

        //Controllo che il primo stato sia validation o dispatching.
        String startState = stateMachine.getCurrentState();
        if(!(startState.equals(TicketStatus.VALIDATION.toString()) ||
                startState.equals(TicketStatus.DISPATCHING.toString()))){

            return "INVALID START STATE : " + startState;
        }

        List<FSMState> states =  stateMachine.getAllStates();
        //Controllo che Ruoli e Stati siano quelli del sistema
        boolean execution = false;
        for(FSMState state : states){
            if(state.getCurrentState().equals(TicketStatus.EXECUTION.toString())){
                execution = true;
            }
            if(!TicketStatus.validateState(state.getCurrentState()))
                return "NOT ADMITTED STATE : " + state.getCurrentState();
            ArrayList<ArrayList<String>> state_info = stateMachine.getStateInformation(state.getCurrentState());
            ArrayList<String> roles = state_info.get(1);
            if(!controlRoles(roles))
                return "NOT ADIMETTED ROLES : " + roles;

            //se la lista dei prossimi stati è nulla sto analizzando l'ultimo stato
            //Controllo che sia CLOSED altrimenti restistuisco false
            if(state_info.get(2).size()==0){
                if(!state.getCurrentState().equals(TicketStatus.CLOSED.toString())) {

                    return "INVALID FINAL STATE : " + state.getCurrentState();
                }
            }

        }

        if(!execution)
            return "NOT EXECUTION STATE IN THE STATE MACHINE";

        //Infine controllo che la macchina a stati non sia spezzata
        if(!controlFSMConnection(stateMachine))
            return "NOT CONNECTED STATE MACHINE";

        return null; // La FSM è valida.
    }

    private boolean controlRoles(ArrayList<String> roles){

        for(String role : roles){
            if(!UserRole.validateRole(role)) {

                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se la macchina a stati è connessa.
     * Cioe' se ogni stati e' raggiungibile dal primo.
     *
     * @param stateMachine la state machine da controllare
     * @return true se è connessa false altrimenti
     */
    @SuppressWarnings("unchecked")
    @Transactional
    private boolean controlFSMConnection(FSM stateMachine){

        List<FSMState> states = stateMachine.getAllStates();
        ArrayList<String> statesString = new ArrayList<>();
        for(FSMState state : states){
            statesString.add(state.getCurrentState());
        }
        //Mappa in cui la chiave è il nome di stato il valore è true se lo stato è raggiungibile
        // false altrimenti
        HashMap<String,Boolean> connectionMap = new HashMap<>();

        //Lo stato di partenza è raggiungibile per definizione
        String startState = stateMachine.getCurrentState();
        connectionMap.put(startState,true);

        for(FSMState state : states){
            //Se lo stato analzizato non è presente nella connectionMap vi è inserito con valore false
            if(!connectionMap.containsKey(state.getCurrentState()))
                connectionMap.put(state.getCurrentState(),false);
            ArrayList<ArrayList<String>> state_info = stateMachine.getStateInformation(state.getCurrentState());
            ArrayList<String> nextStates = state_info.get(2);
            //Analizzo gli successivi i quali sono inseriti nella connectionMap
            //Con valore true;
            for(String nextState : nextStates){
                //Se un next state non è tra gli stati del sistema
                if(!TicketStatus.validateState(nextState)) {

                    return false;
                }
                //Se un next state è negli stati del sistema ma non è presente nell FSM corrente
                if(!statesString.contains(nextState)) {

                    return false;
                }
                connectionMap.remove(nextState);
                connectionMap.put(nextState,true);
            }

        }

        //Infine iterando sulla connectionMap verifico che ogni stato abbia valore true
        //Ovvero sia raggiungbile in caso contrario restituisco false
        for(Map.Entry<String,Boolean> entry : connectionMap.entrySet()){
            if(!entry.getValue()) {

                return false;
            }
        }
        return  true;

    }

    @SuppressWarnings("all")
    @Transactional
    public List<String> getActualStates(String stateMachineName, String role) {
        FSM stateMachine = null;
        ClassPathResource classPathResource = new ClassPathResource("/state_machine/xml_files/");
        String relativePath = null;
        try {
            relativePath = classPathResource.getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(relativePath, stateMachineName);
        String SMPath = file.getPath() + ".xml";
        try {
            stateMachine = new FSM(SMPath, new FSMAction() {
                @Override
                public boolean action(String curState, String message, String nextState, Object args) {
                    System.out.println(curState + ":" + message + " : " + nextState);
                    return true;
                }
            });
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        List<FSMState> fsmStateList = stateMachine.getAllStates();
        ArrayList<String> outPutList = new ArrayList<>();
        for(FSMState fsmState:fsmStateList){
            ArrayList<ArrayList<String>> state_info = stateMachine.getStateInformation(fsmState.getCurrentState());
            ArrayList<String> roleList = state_info.get(1);
            ArrayList<String> stateList = state_info.get(2);
            for(String roleStr : roleList){
                if(roleStr.equals(role)){
                    int index = roleList.indexOf(roleStr);
                    if(!outPutList.contains(stateList.get(index)))
                        outPutList.add(stateList.get(index));
                }
                if(role.equals(UserRole.TEAM_LEADER.toString()) && roleStr.equals(UserRole.TEAM_MEMBER.toString())){
                    int index = roleList.indexOf(roleStr);
                    if(!outPutList.contains(stateList.get(index)))
                        outPutList.add(stateList.get(index));
                }
            }
        }
        return  outPutList;
    }

    @SuppressWarnings("all")
    @Transactional
    public ArrayList<ArrayList<String>> getNextStates(String stateMachineName, String currentState) {
        FSM stateMachine = null;
        ClassPathResource classPathResource = new ClassPathResource("/state_machine/xml_files/");
        String relativePath = null;
        try {
            relativePath = classPathResource.getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(relativePath, stateMachineName);
        String SMPath = file.getPath() + ".xml";
        try {
            stateMachine = new FSM(SMPath, new FSMAction() {
                @Override
                public boolean action(String curState, String message, String nextState, Object args) {
                    System.out.println(curState + ":" + message + " : " + nextState);
                    return true;
                }
            });
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        List<FSMState> fsmStateList = stateMachine.getAllStates();
        ArrayList<ArrayList<String>> outPutList = new ArrayList<>();
        for(FSMState fsmState:fsmStateList){
            if(fsmState.getCurrentState().equals(currentState)) {
                ArrayList<ArrayList<String>> state_info = stateMachine.getStateInformation(fsmState.getCurrentState());
                outPutList.addAll(state_info);
                break;
            }
        }
        return  outPutList;
    }
}
