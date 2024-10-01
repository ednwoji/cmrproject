package CRM.project.service;


import CRM.project.entity.Solutions;
import CRM.project.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static CRM.project.utils.Utils.saveFiles;

@Service
public class SolutionsService {


    @Autowired
    private SolutionRepository solutionRepository;


    public Solutions createSolution(Solutions solutions) {

        String filePath = "/u01/solutions";

        String saveResult = saveFiles(solutions.getFile(), filePath, solutions.getFileName());
        if(saveResult.equalsIgnoreCase("success")) {
            solutions.setFilePath(filePath+solutions.getFileName());
            return solutionRepository.save(solutions);
        }

        else {
            return null;
        }

    }

    public List<Solutions> fetchAllSolutions() {
        return solutionRepository.findAll();
    }
}
