package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.service.JobService;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.server.view.NewJobView;

import java.util.List;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class JobController {

    @Autowired
    private JobService service;

    @GetMapping("/schema/{schemaId}/jobs")
    public List<Job> getAllJobsInCategory(@PathVariable int schemaId) {
        return service.findAllInCategory(schemaId);
    }

    @GetMapping("/jobs/{id}")
    public Job getJob(@PathVariable Integer id) {
        Job job = service.find(id);
        if (job != null)
            return job;
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/jobs")
    public Job createNewJob(@RequestBody NewJobView jobView) {
        Job newJob = service.createNew(new Job.Builder().fromArguments(null, jobView.mappingId(), null, jobView.name(), Job.Type.valueOf(jobView.type()), Job.Status.Ready));
        if (newJob != null)
            return newJob;
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/jobs/{id}/start")
    public Job startJob(@PathVariable int id, HttpSession session) {
        Job job = service.find(id);
        if (job == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not foud.");

        var store = UserStore.fromSession(session);
        return service.start(job, store);
    }

    @PostMapping("/jobs/{id}/startWithQuery")
    public Job startJobWithQuery(@PathVariable int id, HttpSession session, @RequestBody String query) {
        Job job = service.find(id);
        if (job == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not foud.");
        
        job.query = query.substring(1, query.length() - 1);

        var store = UserStore.fromSession(session);
        return service.start(job, store);
    }

    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Integer id) {
        boolean result = service.delete(id);
        if (!result)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
