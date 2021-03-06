package App;

import App.authentication.JwtAuthenticationRequest;
import App.authentication.JwtAuthenticationResponse;
import App.authentication.JwtTokenUtil;
import App.authentication.UserService;
import App.logic.CheckResponse;
import App.logic.GetWordsResponse;
import App.logic.SaveWordList;
import App.logic.Tools;
import App.registration.UserValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import model.Entry;
import model.User;
import model.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.expression.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@CrossOrigin
@RestController
public class MainController {
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private UserValidator userValidator;

    /**
     * Returns message for diagnostics purposes.
     * @return "Dit is de API van TeamMartini voor ScrumProject AdvProg"
     */
    @RequestMapping("/")
    public String returnMsg() {

        return "Dit is de API van TeamMartini voor ScrumProject AdvProg";
    }

    /**
     * Returns List of all database entries.
     * @return list of entries
     */

    @CrossOrigin
    @RequestMapping("/Entries")
    public Wrapper<List<Entry>> getEntries() {
        //DB-connection
        MongoOperations mongoOperations = Tools.getMongoOperations();

        List<Entry>  entries = new ArrayList<Entry>();  
        for (Entry entry : mongoOperations.findAll(Entry.class, "entries")) {
            entries.add(entry);
            System.out.println("entry found");
        }
        return new Wrapper<List<model.Entry>>(true,"get data succeeded",entries);
    }

    
    /**
     * Returns List of set size with random words.
     * @param amount amount of words
     * @param from language to translate from
     * @param to language to translate to
     * @return list of entries
     */

    @CrossOrigin
    @GetMapping("/Exercise")
    public Wrapper exerciseForm(
            @RequestParam(value="amount", defaultValue="10") String amount,
            @RequestParam(value="from", defaultValue="") String from,
            @RequestParam(value="to", defaultValue="") String to) throws JsonProcessingException {
        return new GetWordsResponse(new String[] { from, to }, amount).listOut();
    }
    
    /**
     * Returns score when list of entries is given.
     * @param input list of entries
     * @return score, maximum score, list of corrections.
     * @throws ParseException
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, value = "/Exercise")
    public Wrapper exerciseSubmit(@RequestBody String input, @RequestParam("token") String token) throws ParseException {
        //if (token == null) throw new IllegalArgumentException("token is null in exercisecontroller");

        String user = SecurityContextHolder.getContext().getAuthentication().getName().toString();

        return new CheckResponse(user, input).getResult();
    }

    /**
     * Saves user made list.
     * @param input user made list
     * @return failed or success
     * @throws ParseException
     */
    @CrossOrigin
    @PostMapping("/SaveList")
    public Wrapper saveList(@RequestBody String input) throws ParseException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return new SaveWordList(user, input).getConfirmation();
    }


    /**
     * Shows the login page
     * @return "Login page"
     **/
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(){
        //UsernamePasswordAuthenticationToken sf = new UsernamePasswordAuthenticationToken("name", "password");
        return "Login page";
    }

    /**
     *   Loaded when user tries to login
     *
     *   if successful: return a jwt
     *   if fail: return badcredentialserror
     *   @param authenticationRequest
     *   @return
     **/
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {
        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    /**
     * Register user
     * @param user
     * @param br
     * @param response
     * @return
     **/
    @CrossOrigin
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String createUser(@Valid User user, BindingResult br, HttpServletResponse response) {
        userValidator.validate(user, br);
        if (br.hasErrors()) {
            return br.toString();
        }

        System.out.println(user.getUsername());
        userDetailsService.save(user);

        return "success";
    }
}
