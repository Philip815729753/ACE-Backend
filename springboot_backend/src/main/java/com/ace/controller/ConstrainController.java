package com.ace.controller;


import com.ace.service.ConstrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//Complete CRUD for constrains
@RestController
@RequestMapping("/constrain")
public class ConstrainController {

    @Autowired
    private ConstrainService constrainService;

    // Handle POST request for adding constrain
    @PostMapping("/add")
    public String addConstrain(@RequestParam("constrain_name") String constrainName,
                               @RequestParam("team1_code") int team1Code,
                               @RequestParam(value = "team2_code", required = false) Integer team2Code, // Use Integer for nullable value
                               @RequestParam("competition_name") String competitionName,
                               @RequestParam(value = "round",required = false) Integer round)

    {
        int constrainType = constrainService.getConstrainType(constrainName);
        //if wrong format for special request(assign one team play in a particular round), return error message
        if(constrainType==3){
            if(!(team2Code==null&&round!=null&&round>0&&round<15)){
                return "Wrong format for constraint,please ensure that " +
                        "you have entered a valid round number " +
                        "and don't choose team2";
            }
        }else if(team2Code==null||round!=null){
            return "Wrong format for constraint";
        }
        try {
            // Call the service to add constrain
            constrainService.addConstrain(constrainType, team1Code, team2Code, competitionName,round);
            return "Constraint is added successfully!";
        } catch (Exception e) {
            return "Constraint cannot be added!";
        }
    }

    @DeleteMapping("/remove")
    public String removeConstrain(@RequestParam("constrain_name") String constrainName,
                               @RequestParam("team1_code") int team1Code,
                               @RequestParam(value = "team2_code", required = false) Integer team2Code, // Use Integer for nullable value
                               @RequestParam("competition_name") String competitionName,
                               @RequestParam(value = "round",required = false) Integer round)

    {
        int constrainType = constrainService.getConstrainType(constrainName);
        try {
            // Call the service to add constrain
            constrainService.removeConstrain(constrainType, team1Code, team2Code, competitionName,round);
            return "Constraint is removed successfully!";
        } catch (Exception e) {
            return "Constraint cannot be removed!";
        }
    }

    @PutMapping("/edit")
    public String editConstrain(@RequestParam("constrain_name") String constrainName,
                               @RequestParam("team1_code") int team1Code,
                               @RequestParam(value = "team2_code", required = false) Integer team2Code, // Use Integer for nullable value
                               @RequestParam("competition_name") String competitionName,
                               @RequestParam(value = "round",required = false) Integer round)

    {
        int constrainType = constrainService.getConstrainType(constrainName);
        //if wrong format for special request3(assign one team play in a particular round), return error message
        if(constrainType==3){
            if(!(team2Code==null&&round!=null&&round>0&&round<15)){
                return "Wrong format for constraint,please ensure that " +
                        "you have entered a valid round number " +
                        "and don't choose team2";
            }
        }else if(team2Code==null||round!=null){
            return "Wrong format for constraint";
        }
        try {
            // Call the service to add constrain
            constrainService.editConstrain(constrainType, team1Code, team2Code, competitionName,round);
            return "Constraint is edited successfully!";
        } catch (Exception e) {
            return "Constraint cannot be edited!";
        }
    }
}


