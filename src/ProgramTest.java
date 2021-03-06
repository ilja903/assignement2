
import com.google.java.contract.PostconditionError;
import com.google.java.contract.PreconditionError;
import org.junit.Before;
import org.junit.Test;


public class ProgramTest {

    Bugzilla bugzilla;
    Bug bug;

    public ProgramTest() {
        try {
            BugzillaException.init();
        } catch (BugzillaException e) {
            e.printStackTrace();
            System.out.println(e.getErrorMsg());
        }
    }

    @Before
    public void setUp() throws Exception {
        bugzilla = new Bugzilla(false);
        bug = bug();
    }

/// Bugzilla

    @Test(expected = PreconditionError.class)
    public void shouldFailIfRegisterHasNullValues1() throws Exception {
        bugzilla.register(null, "asd", Bugzilla.MemberType.DEVELOPER);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfRegisterHasNullValues2() throws Exception {
        bugzilla.register("asd", null, Bugzilla.MemberType.DEVELOPER);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfLoginUerDoesNotExist() throws Exception {
        bugzilla.login("asdasd", "asd");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfLoginPasswordIsIncorrect() throws Exception {
        bugzilla.register("asdasd", "asd", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("asdasd", "asd1");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfLoginHasNullValue1() throws Exception {
        bugzilla.login(null, "asd");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfLoginHasNullValue2() throws Exception {
        bugzilla.login("asd", null);
    }

    @Test
    public void shouldLogout() throws Exception {
        bugzilla.register("asdasd", "asd", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("asdasd", "asd");
        bugzilla.logout("asdasd");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailLogoutBecauseOfNull() throws Exception {
        bugzilla.register("asdasd", "asd", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("asdasd", "asd");
        bugzilla.logout(null);
    }

    @Test
    public void shouldSubmitBug() throws Exception {
        bugzilla.register("username", "pass", Bugzilla.MemberType.USER);
        bugzilla.login("username", "pass");
        bugzilla.submitBug("username", "description");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotSubmitBugBecauseNotLoggedIn() throws Exception {
        bugzilla.register("username", "pass", Bugzilla.MemberType.USER);
        bugzilla.submitBug("username", "description");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotSubmitBugBecauseDescriptionLengthIsZero() throws Exception {
        bugzilla.register("username", "pass", Bugzilla.MemberType.USER);
        bugzilla.submitBug("username", "");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotSubmitBugBecauseOfBadRole() throws Exception {
        bugzilla.register("username", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("username", "pass");
        bugzilla.submitBug("username", "description");
    }

    @Test
    public void shouldConfirmBug() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotConfirmBugBecauseOfBadRole() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("user", 0);
    }

    @Test
    public void shouldInvalidateBug() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.invalidateBug("analyst", 0, "This is not a bug at all!");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotInvalidateBugBecauseOfBadRole() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.invalidateBug("user", 0, "This is not a bug at all!");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotInvalidateBugBecauseOfNull() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.invalidateBug(null, 0, "This is not a bug at all!");
    }

    @Test
    public void shouldIStartDevelopment() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
    }


    @Test(expected = PreconditionError.class)
    public void shouldNotStartDevelopmentBecauseOfWrongFlow() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.startDevelopment("analyst", 0);
    }

    @Test
    public void shouldStopDevelopment() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.stopDevelopment("developer", 0);
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotStopDevelopmentBecauseOfDeveloperIsNotSignedIn() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);

        bugzilla.logout("developer");

        bugzilla.stopDevelopment("developer", 0);
    }

    @Test
    public void shouldFixBug() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("developer", 0, Bug.Resolution.WONTFIX, "solution");
    }

    @Test(expected = PreconditionError.class)
    public void shouldNotFixBugBecauseBugCanBeFixedOnlyByDeveloper() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("user", 0, Bug.Resolution.WONTFIX, "solution");
    }

    @Test
    public void shouldApproveFix() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.register("qa", "pass", Bugzilla.MemberType.QUALITYASSURANCE);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.login("qa", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("developer", 0, Bug.Resolution.FIXED, "solution");
        bugzilla.approveFix("qa", 0);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailBecauseProgrammerCannotApproveFix() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.register("qa", "pass", Bugzilla.MemberType.QUALITYASSURANCE);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.login("qa", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("developer", 0, Bug.Resolution.FIXED, "solution");
        bugzilla.approveFix("developer", 0);
    }

    @Test
    public void shouldRejectFix() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.register("qa", "pass", Bugzilla.MemberType.QUALITYASSURANCE);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.login("qa", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("developer", 0, Bug.Resolution.FIXED, "solution");
        bugzilla.rejectFix("qa", 0);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailBecauseProgrammerCannotRejectFix() throws Exception {
        bugzilla.register("user", "pass", Bugzilla.MemberType.USER);
        bugzilla.register("analyst", "pass", Bugzilla.MemberType.SYSTEMANALYST);
        bugzilla.register("developer", "pass", Bugzilla.MemberType.DEVELOPER);
        bugzilla.register("qa", "pass", Bugzilla.MemberType.QUALITYASSURANCE);
        bugzilla.login("user", "pass");
        bugzilla.login("analyst", "pass");
        bugzilla.login("developer", "pass");
        bugzilla.login("qa", "pass");
        bugzilla.submitBug("user", "description");
        bugzilla.confirmBug("analyst", 0);
        bugzilla.startDevelopment("developer", 0);
        bugzilla.fixedBug("developer", 0, Bug.Resolution.FIXED, "solution");
        bugzilla.rejectFix("developer", 0);
    }


    /// Bug

    @Test(expected = PreconditionError.class)
    public void shouldFailIfGoesFromUnconfirmedToVerified() throws Exception {
        bug.setState(Bug.State.VERIFIED);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfGoesFromVerifiedToConfirmed() throws Exception {
        bug.setState(Bug.State.VERIFIED);
        bug.setState(Bug.State.CONFIRMED);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfGoesFromConfirmedToResolved() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfGoesFromConfirmedToVerified() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
        bug.setState(Bug.State.VERIFIED);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfGoesFromVerifiedToUnconfirmed() throws Exception {
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
        bug.setState(Bug.State.VERIFIED);
        bug.setState(Bug.State.UNCONFIRMED);
    }

    @Test
    public void shouldGoFromUnconfirmedToConfirmed() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailGoesFromUnconfirmedToResolved() throws Exception {
        bug.setState(Bug.State.RESOLVED);
    }

    @Test
    public void shouldGoFromConfirmedToInProgress() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
        bug.setState(Bug.State.INPROGRESS);
    }

    @Test
    public void shouldGoFromInProgressToResolved() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
        bug.setState(Bug.State.INPROGRESS);
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
    }

    @Test
    public void shouldGoFromResolvedToVerified() throws Exception {
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
        bug.setState(Bug.State.VERIFIED);
    }

    @Test
    public void shouldGoFromInProgressToConfirmed() throws Exception {
        bug.setState(Bug.State.CONFIRMED);
        bug.setState(Bug.State.INPROGRESS);
    }

    @Test
    public void shouldGoFromResolvedToConfirmed() throws Exception {
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
        bug.setState(Bug.State.CONFIRMED);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfResolutionIsEmpty() throws Exception {
        bug.setAsResolved(Bug.Resolution.FIXED, "");
        bug.setAsResolved(Bug.Resolution.FIXED, null);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfResolutionIsUnresolved() throws Exception {
        bug.setAsResolved(Bug.Resolution.UNRESOLVED, "12313");
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfBugWithEmptyDescription() throws Exception {
        new Bug(12, "");
        new Bug(12, null);
    }

    @Test(expected = PreconditionError.class)
    public void shouldFailIfModifiedAfterVerified() throws Exception {
        bug.setAsResolved(Bug.Resolution.FIXED, "123");
        bug.setState(Bug.State.VERIFIED);
        bug.setAsResolved(Bug.Resolution.FIXED, "q23234");
    }

    @Test(expected = PreconditionError.class)
    public void testRegisterNullName() throws BugzillaException {
        new Bugzilla(false).register(null, "abc", Bugzilla.MemberType.USER);
    }

    @Test(expected = PreconditionError.class)
    public void testChangeStateUnconfirmedToInProgress() throws BugzillaException {
        new Bug(0, "crash on OK press").setState(Bug.State.INPROGRESS);
    }

    private Bug bug() throws BugzillaException {
        return new Bug(5, "testBug");
    }

}
