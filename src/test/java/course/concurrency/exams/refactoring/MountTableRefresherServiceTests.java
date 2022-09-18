package course.concurrency.exams.refactoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class MountTableRefresherServiceTests {

    private MountTableRefresherService service;

    private Others.RouterStore routerStore;
    private Others.MountTableManager manager;
    private Others.LoadingCache routerClientsCache;
    private Others.MountTableManager managerFail;


    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        routerStore = mock(Others.RouterStore.class);
        manager = mock(Others.MountTableManager.class);
        managerFail = mock(Others.MountTableManager.class);
        service.setRouterStore(routerStore);
        routerClientsCache = mock(Others.LoadingCache.class);
        service.setRouterClientsCache(routerClientsCache);
        // service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
        // service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        List<Others.RouterState> states = addresses.stream()
                .map(Others.RouterState::new).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        List<MountTableRefresherThread> collect = addresses
                .stream()
                .map(address -> new MountTableRefresherThread(manager, address))
                .collect(toList());

        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(0));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(1));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(2));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(3));

        when(manager.refresh()).thenReturn(true);

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=4,failureCount=0");
        verify(manager, times(4)).refresh();
        verify(routerClientsCache, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("All tasks failed")
    public void noSuccessfulTasks() {

        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        List<Others.RouterState> states = addresses.stream()
                .map(Others.RouterState::new).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        List<MountTableRefresherThread> collect = addresses
                .stream()
                .map(address -> new MountTableRefresherThread(manager, address))
                .collect(toList());

        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(0));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(1));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(2));
        when(mockedService.createMountTableRefreshThread(anyString())).thenReturn(collect.get(3));

        when(manager.refresh()).thenReturn(false);

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(manager, times(4)).refresh();
        verify(routerClientsCache, times(4)).invalidate(anyString());
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSuccessedTasks() {

        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        List<Others.RouterState> states = addresses.stream()
                .map(Others.RouterState::new).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        when(mockedService.createMountTableRefreshThread(states.get(0).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "1231"));
        when(mockedService.createMountTableRefreshThread(states.get(1).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "local"));
        when(mockedService.createMountTableRefreshThread(states.get(2).getAdminAddress())).thenReturn(new MountTableRefresherThread(managerFail, "789"));
        when(mockedService.createMountTableRefreshThread(states.get(3).getAdminAddress())).thenReturn(new MountTableRefresherThread(managerFail, "local"));

        when(manager.refresh()).thenReturn(true);
        when(managerFail.refresh()).thenReturn(false);

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=2,failureCount=2");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(manager, times(2)).refresh();
        verify(managerFail, times(2)).refresh();
        verify(routerClientsCache, times(2)).invalidate(anyString());

    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        List<Others.RouterState> states = addresses.stream()
                .map(Others.RouterState::new).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        when(mockedService.createMountTableRefreshThread(states.get(0).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "1231"));
        when(mockedService.createMountTableRefreshThread(states.get(1).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "local"));
        when(mockedService.createMountTableRefreshThread(states.get(2).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "789"));
        when(mockedService.createMountTableRefreshThread(states.get(3).getAdminAddress())).thenReturn(new MountTableRefresherThread(managerFail, "local"));

        when(manager.refresh()).thenReturn(true);
        when(managerFail.refresh()).thenThrow(new NullPointerException());

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(manager, times(3)).refresh();
        verify(managerFail, times(1)).refresh();
        verify(routerClientsCache, times(1)).invalidate(anyString());
    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        List<Others.RouterState> states = addresses.stream()
                .map(Others.RouterState::new).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        when(mockedService.createMountTableRefreshThread(states.get(0).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "1231"));
        when(mockedService.createMountTableRefreshThread(states.get(1).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "local"));
        when(mockedService.createMountTableRefreshThread(states.get(2).getAdminAddress())).thenReturn(new MountTableRefresherThread(manager, "789"));
        when(mockedService.createMountTableRefreshThread(states.get(3).getAdminAddress())).thenReturn(new MountTableRefresherThread(managerFail, "local"));

        when(manager.refresh()).thenReturn(true);
        when(managerFail.refresh()).thenAnswer(invocationOnMock -> {
            Thread.sleep(3000);
            return true;
        });

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
        verify(mockedService).log("Not all router admins updated their cache");
        verify(manager, times(3)).refresh();
        verify(managerFail, times(1)).refresh();
        verify(routerClientsCache, times(1)).invalidate(anyString());
    }

}
