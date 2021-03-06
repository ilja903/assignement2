import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.java.contract.ThrowEnsures;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;


public class Bugzilla implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum MemberType {
        SYSTEMANALYST,
        QUALITYASSURANCE,
        DEVELOPER,
        USER
    }

    @Requires({
            "username != null",
            "passwd != null",
            "type != null",
            "isRegistered(username) == false"
    })
    @ThrowEnsures({
            "BugzillaException", "isRegistered(old(username))",
            "BugzillaException", "username != null",
            "BugzillaException", "passwd != null",
    })
    public void register(String username, String passwd, MemberType type) throws BugzillaException {

        if (username == null) {
            throwBex(BugzillaException.ErrorType.USERNAME_NULL);
        }

        if (passwd == null) {
            throwBex(BugzillaException.ErrorType.PASSWORD_NULL);
        }

        if (isRegistered(username)) {
            throwBex(BugzillaException.ErrorType.USER_ALREADY_REGISTRED);
        }

        members.put(username, getMember(passwd, type));
    }


    @Requires({
            "username != null",
            "passwd != null",
            "members.containsKey(username) == true",
            "getPasswd(username).equals(passwd)"
    })
    @ThrowEnsures({
            "BugzillaException", "!isLoggedIn(username)"
    })
    public void login(String username, String passwd) throws BugzillaException {
        loggedIn.add(username);

        if (!isLoggedIn(username)) {
            throwBex(BugzillaException.ErrorType.LOGIN_FAILED);
        }
    }


    @Requires({
            "username != null"
    })
    @ThrowEnsures({
            "BugzillaException", "isLoggedIn(username)"
    })
    public void logout(String username) throws BugzillaException {

        loggedIn.remove(username);

        if (isLoggedIn(username)) {
            throwBex(BugzillaException.ErrorType.LOGOUT_FAILED);
        }
    }

    @Requires({
            "username != null",
            "description != null",
            "description.length() > 0",
            "getType(username) == MemberType.USER",
            "isLoggedIn(username)"
    })
    @Ensures({
            "bugCount() == old(bugCount()) + 1",
            //...
    })
    @ThrowEnsures({
            "BugzillaException", "username == null",
            "BugzillaException", "getType(username) != MemberType.USER"
    })
    /*
     * The method allows a USER to submit a new bug
	 */
    public void submitBug(String username, String description) throws BugzillaException {

        if (username == null) {
            throwBex(BugzillaException.ErrorType.USERNAME_NULL);
        }

        if (getType(username) != MemberType.USER) {
            throwBex(BugzillaException.ErrorType.USER_ACTION_NOT_PERMITTED);
        }

        int bugID = bugs.size();
        bugs.put(bugID, new Bug(bugID, description));
    }


    @Requires({
            "username != null",
            "getType(username) == MemberType.SYSTEMANALYST",
            "isLoggedIn(username)",
            "bugExists(bugID)"
    })
    @Ensures({
            "getBug(bugID).getState() == Bug.State.CONFIRMED"
    })
    @ThrowEnsures({
            "BugzillaException", "getBug(bugID).getState() != Bug.State.CONFIRMED",
    })
    /*
     * The method allows a SYSTEMANALYST to confirm a bug
     */
    public void confirmBug(String username, int bugID) throws BugzillaException {

        getBug(bugID).setState(Bug.State.CONFIRMED);

        if (getBug(bugID).getState() != Bug.State.CONFIRMED) {
            throwBex(BugzillaException.ErrorType.TRANSITION_TO_CONFIRMED_STATE_UNSUCCESSFUL);
        }
    }


    @Requires({
            "username != null",
            "username.length() > 0",
            "solution != null",
            "solution.length() > 0",
            "getType(username) == MemberType.SYSTEMANALYST",
            "isLoggedIn(username)",
            "bugExists(bugID)"
    })
    @ThrowEnsures({
            "BugzillaException", "getBug(bugID).getState() != Bug.State.RESOLVED",
    })
    /*
     * The method allows a SYSTEMANALYST to invalidate a bug
     */
    public void invalidateBug(String username, int bugID, String solution) throws BugzillaException {
        getBug(bugID).setAsResolved(Bug.Resolution.INVALID, solution);

        if (getBug(bugID).getState() != Bug.State.RESOLVED) {
            throwBex(BugzillaException.ErrorType.TRANSITION_TO_CONFIRMED_STATE_UNSUCCESSFUL);
        }
    }


    @Requires({
            "username != null",
            "username.length() > 0",
            "getType(username) == MemberType.DEVELOPER",
            "isLoggedIn(username)",
            "bugExists(bugID)"
    })
    @ThrowEnsures({
            "BugzillaException", "!isDeveloperAssigned(username)",
            "BugzillaException", "!devInProgress(username, bugID)",
            "BugzillaException", "getBug(bugID).getState() != Bug.State.INPROGRESS",
    })
    /*
     * The method allows a DEVELOPER to start working on the bug
     */
    public void startDevelopment(String username, int bugID) throws BugzillaException {
        getBug(bugID).setState(Bug.State.INPROGRESS);
        inProgress.put(username, bugID);

        if (!isDeveloperAssigned(username)) {
            throwBex(BugzillaException.ErrorType.BUG_WAS_NOT_ASSIGNED_TO_DEVELOPER);
        }

        if (!devInProgress(username, bugID)) {
            throwBex(BugzillaException.ErrorType.BUG_WAS_NOT_ASSIGNED_TO_DEVELOPER);
        }

        if (getBug(bugID).getState() != Bug.State.INPROGRESS) {
            throwBex(BugzillaException.ErrorType.TRANSITION_TO_INPROGRESS_STATE_UNSUCCESSFUL);
        }
    }


    @Requires({
            "username != null",
            "username.length() > 0",
            "getType(username) == MemberType.DEVELOPER",
            "isLoggedIn(username)",
            "bugExists(bugID)"
    })
    @ThrowEnsures({
            "BugzillaException", "isDeveloperAssigned(username)",
            "BugzillaException", "getBug(bugID).getState() != Bug.State.CONFIRMED"
    })
    /*
     * The method allows a DEVELOPER to stop working on the bug
     */
    public void stopDevelopment(String username, int bugID) throws BugzillaException {
        getBug(bugID).setState(Bug.State.CONFIRMED);
        inProgress.remove(username);

        if (isDeveloperAssigned(username)) {
            throwBex(BugzillaException.ErrorType.BUG_IS_STILL_ASSIGNED_TO_DEVELOPER);
        }

        if (getBug(bugID).getState() != Bug.State.CONFIRMED) {
            throwBex(BugzillaException.ErrorType.TRANSITION_TO_CONFIRMED_STATE_UNSUCCESSFUL);
        }
    }


    @Requires({
            "username != null",
            "username.length() > 0",
            "resType != null",
            "bugExists(bugID)",
            "solution != null",
            "solution.length() > 0",
            "isLoggedIn(username)",
            "bugExists(bugID)",
            "getType(username) == MemberType.DEVELOPER"
    })
    @ThrowEnsures({
            "BugzillaException", "isDeveloperAssigned(username)",
    })
    /*
     * The method allows DEVELOPER to mark the bug as fixed
     */
    public void fixedBug(String username, int bugID, Bug.Resolution resType, String solution) throws BugzillaException {
        getBug(bugID).setAsResolved(resType, solution);
        inProgress.remove(username);

        if (isDeveloperAssigned(username)) {
            throwBex(BugzillaException.ErrorType.BUG_IS_STILL_ASSIGNED_TO_DEVELOPER);
        }
    }


    /*
     * The method allows QUALITYASSURANCE to approve the fix (VERIFY)
     */
    @Requires({
            "username != null",
            "username.length() > 0",
            "isLoggedIn(username)",
            "bugExists(bugID)",
            "getType(username) == MemberType.QUALITYASSURANCE"
    })
    @ThrowEnsures({
            "BugzillaException", "isDeveloperAssigned(username)",
            "BugzillaException", "getBug(bugID).getState() != Bug.State.VERIFIED"
    })
    public void approveFix(String username, int bugID) throws BugzillaException {
        getBug(bugID).setState(Bug.State.VERIFIED);

        if (isDeveloperAssigned(username)) {
            throwBex(BugzillaException.ErrorType.BUG_IS_STILL_ASSIGNED_TO_DEVELOPER);
        }

        if (getBug(bugID).getState() != Bug.State.VERIFIED) {
            throwBex(BugzillaException.ErrorType.BUG_IS_STILL_ASSIGNED_TO_DEVELOPER);
        }
    }


    /*
     * The method allows QUALITYASSURANCE to reject the bug (back to CONFIRMED)
     */
    @Requires({
            "username != null",
            "username.length() > 0",
            "bugExists(bugID)",
            "isLoggedIn(username)",
            "bugExists(bugID)",
            "getType(username) == MemberType.QUALITYASSURANCE"
    })
    @ThrowEnsures({
            "BugzillaException", "getBug(bugID).getState() != Bug.State.CONFIRMED"
    })
    public void rejectFix(String username, int bugID) throws BugzillaException {
        getBug(bugID).setState(Bug.State.CONFIRMED);

        if (getBug(bugID).getState() != Bug.State.CONFIRMED) {
            throwBex(BugzillaException.ErrorType.TRANSITION_TO_CONFIRMED_STATE_UNSUCCESSFUL);
        }
    }


    /*
     * Method for throwing exception
     */
    public static void throwBex(BugzillaException.ErrorType type) throws BugzillaException {
        throw new BugzillaException(type);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /*
	 * The following private methods can be used for the task
	 */

    private MemberType getType(String username) {
        return members.get(username).getRight();

    }

    private String getPasswd(String username) {
        return members.get(username).getLeft();
    }

    private boolean isRegistered(String username) {
        return members.containsKey(username);

    }

    private boolean isLoggedIn(String username) {
        return loggedIn.contains(username);

    }

    private boolean bugExists(int bugID) {
        return bugs.containsKey(bugID);

    }

    private int bugCount() {
        return bugs.size();
    }

    /*
     * The method returns the Id of the bug that was created latest
     */
    private int lastBugID() {
        return (bugs.size() - 1);

    }

    /*
     * The method returns the Bug object with a given bug ID
     */
    private Bug getBug(int bugID) {
        return bugs.get(bugID);

    }
	
	/*
	 * The method checks if a developer is already assigned to a bug.
	 * When a developer changes the state of an object to INPROGRESS,
	 * then s/he is consider assigned to the bug.
	 * When the state of the bug is changed by the same developer to 
	 * CONFIRMED (stop working) or RESOLVED (fixed bug) then s/he is not
	 * considered to be assigned.
	 */

    private boolean isDeveloperAssigned(String username) {
        return inProgress.containsKey(username);

    }
	
	/*
	 * The method checks if a developer is is assigned to a specific 
	 * bug ID
	 */

    private boolean devInProgress(String username, int bugID) {
        return (inProgress.get(username) == bugID);
    }

///////////////////////////////////////////////////////////////////////////////////////
	/*
	 * The following methods are not relevant for the assignment task
	 */

    @Ensures({
            "exceptionsInitialized() == true",
            "dataInitialised() == true",
            "fileEnabled == old(saveToFile)",
            "fileEnabled? fileExists() == true: true"
    })
	/*
	 * The constructor initializes the loads and initializes the data
	 * The file operations are enabled only if saveToFile is true.
	 */
    public Bugzilla(boolean saveToFile) throws BugzillaException {

        fileEnabled = saveToFile;
        BugzillaException.init();

        loggedIn = new ArrayList<String>();

        if (!fileEnabled) {
            bugs = new HashMap<Integer, Bug>();
            members = new HashMap<String, Pair<String, MemberType>>();
            inProgress = new HashMap<String, Integer>();
        } else {
            try {
                loadDB();
            } catch (Exception e1) {
                if (fileExists()) {
                    File f = new File(filePath);
                    if (!f.delete()) {
                        throwBex(BugzillaException.ErrorType.DB_LOAD_ERROR);
                    }
                }

                bugs = new HashMap<Integer, Bug>();
                members = new HashMap<String, Pair<String, MemberType>>();
                inProgress = new HashMap<String, Integer>();

                try {
                    saveDB();
                } catch (Exception e2) {
                    if (fileExists()) {
                        File f = new File(filePath);
                        f.delete();
                    }
                    throwBex(BugzillaException.ErrorType.DB_LOAD_ERROR);
                }
            }
        }

    }

    @Ensures({
            "isCopyOf(result) == true"
    })
    public Map<Integer, Bug> getBugList() {
        return Collections.unmodifiableMap(bugs);
    }

    private Pair<String, MemberType> getMember(String passwd, MemberType type) {
        return new ImmutablePair<String, MemberType>(passwd, type);
    }

    private boolean isCopyOf(Map<Integer, Bug> map) {
        return map.equals(bugs);
    }

    public void saveData() throws BugzillaException {
        if (fileEnabled) {
            try {
                saveDB();
            } catch (Exception ex) {
                ex.printStackTrace();
                throwBex(BugzillaException.ErrorType.DB_SAVE_ERROR);

            }
        }
    }

    private void saveDB() throws Exception {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(members);
            out.writeObject(bugs);
            out.writeObject(inProgress);

            out.close();
            fileOut.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;

        }

    }


    @SuppressWarnings("unchecked")
    private void loadDB() throws Exception {

        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            members = (Map<String, Pair<String, MemberType>>) in.readObject();
            bugs = (Map<Integer, Bug>) in.readObject();
            inProgress = (Map<String, Integer>) in.readObject();

            in.close();
            fileIn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private boolean fileExists() {
        File f = new File(filePath);
        return (f.exists() && !f.isDirectory());
    }

    private boolean dataInitialised() {
        return (members != null &&
                loggedIn != null &&
                bugs != null &&
                inProgress != null);
    }

    private boolean exceptionsInitialized() {
        return BugzillaException.exInitialized();
    }


    private Map<String, Pair<String, MemberType>> members;
    private ArrayList<String> loggedIn;
    private Map<String, Integer> inProgress;
    private Map<Integer, Bug> bugs;

    private boolean fileEnabled;

    private static final String filePath = "bl.bin";
}
