package pl.llp.aircasting.sessionSync;

import pl.llp.aircasting.networking.drivers.SessionDriver;
import pl.llp.aircasting.networking.schema.DeleteSessionResponse;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.networking.httpUtils.HttpResult;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SyncServiceTest
{
  SyncService service;
  List<Session> sessions;
  private HttpResult<DeleteSessionResponse> result;

  @Before
  public void setUp() throws Exception
  {
    sessions = newArrayList();
    service = new SyncService();
    service.sessionDriver = mock(SessionDriver.class);
    service.sessionRepository = mock(SessionRepository.class);

    result = mock(HttpResult.class);
    when(result.getContent()).thenReturn(new DeleteSessionResponse(Boolean.TRUE));

    when(service.sessionDriver.deleteSession(Matchers.<Session>any())).thenReturn(result);
  }

  @Test
  public void testPrepareSessions() throws Exception
  {
    service.prepareSessions(sessions);
  }

  @Test
  public void should_try_to_delete_sessions_marked_for_deletion() throws Exception
  {
    // given
    Session session = new Session();
    session.setMarkedForRemoval(true);
    sessions.add(session);

    // when
    List<Session> withoutDeleted = service.prepareSessions(sessions);

    // then
    verify(service.sessionDriver).deleteSession(Mockito.any(Session.class));
    assertThat(withoutDeleted).isEmpty();
  }
}
