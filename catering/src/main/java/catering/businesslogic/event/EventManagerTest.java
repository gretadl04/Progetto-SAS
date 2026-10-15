package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Time;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

@TestMethodOrder(OrderAnnotation.class)
public class EventManagerTest {

    private static CatERing app;
    private static User organizer;
    private static User chef;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        // Carica un utente Organizzatore (es. id 1 o cerca nel DB) e gli assicura il ruolo
        organizer = User.load(1);
        if (organizer != null && !organizer.isOrganizer()) {
            organizer.addRole(User.Role.ORGANIZER);
        }
        // Carica uno Chef (es. id 2 - Antonio o simile)
        chef = User.load(2);
        if (chef != null && !chef.isChef()) {
            chef.addRole(User.Role.CHEF);
        }
        app.getUserManager().fakeLogin(organizer.getUserName());
    }

    @Test
    @Order(1)
    void testMainScenarioCreateAndConfirmEvent() throws UseCaseLogicException {
        EventManager em = app.getEventManager();

        // 1. Crea evento
        Date start = Date.valueOf("2026-06-10");
        Date end = Date.valueOf("2026-06-11");
        Event ev = em.createEvent("Matrimonio Reale", "Castello di Rivoli", start, end);

        assertNotNull(ev);
        assertTrue(ev.getId() > 0);
        assertEquals("in lavorazione", ev.getState());

        // 2. Assegna Chef
        em.assignChef(chef);
        assertEquals(chef.getId(), ev.getChefId());

        // 3. Definisce servizio
        Service srv = em.createService("Pranzo Nuziale", start, Time.valueOf("12:30:00"), Time.valueOf("16:30:00"), "Salone Reale");
        assertNotNull(srv);
        assertTrue(ev.getServices().contains(srv));

        // 4. Definisce turno e richiede personale
        StaffRequest req = em.addShiftAndRequestStaff(start, Time.valueOf("11:00:00"), Time.valueOf("17:00:00"), "Cameriere", 6);
        assertNotNull(req);
        assertEquals(1, srv.getStaffRequests().size());
        assertEquals("Cameriere", req.getRole());
        assertEquals(6, req.getQuantity());

        // 5. Conferma evento
        em.confirmEvent();
        assertEquals("programmato", ev.getState());

        // Verifica persistenza DB
        Event loaded = Event.loadById(ev.getId());
        assertNotNull(loaded);
        assertEquals("programmato", loaded.getState());
        assertEquals("Castello di Rivoli", loaded.getLocation());
    }

    @Test
    @Order(2)
    void testRecurrenceAndSingleInstanceModification() throws UseCaseLogicException {
        EventManager em = app.getEventManager();

        Date start = Date.valueOf("2026-07-01");
        Date end = Date.valueOf("2026-07-01");
        Event ev = em.createEvent("Coffee Break Aziendale", "Sede Torino", start, end);

        // Estensione 1b: Evento ricorrente settimanale per 3 settimane
        Date recEnd = Date.valueOf("2026-07-15");
        em.defineRecurrence("WEEKLY", recEnd);

        assertTrue(ev.isRecurring());
        assertEquals(3, ev.getInstances().size());

        // Estensione 1b.1: Modifica di una singola istanza
        EventInstance secondInstance = ev.getInstances().get(1);
        em.modifyRecurrenceEvents(secondInstance, false, "Sede Milano", "annullato");

        assertTrue(secondInstance.isModified());
        assertEquals("Sede Milano", secondInstance.getCustomLocation());
        assertEquals("annullato", secondInstance.getState());
        // Verifica che la prima istanza sia rimasta invariata
        assertEquals("programmato", ev.getInstances().get(0).getState());
    }

    @Test
    @Order(3)
    void testApproveMenuWithModifications() throws UseCaseLogicException {
        EventManager em = app.getEventManager();

        Event ev = em.createEvent("Cena di Gala", "Villa d'Este", Date.valueOf("2026-08-20"), Date.valueOf("2026-08-20"));
        Service srv = em.addService("Cena Placée");

        Menu menu = new Menu(chef, "Menu Degustazione Estate");
        menu.saveNewMenu();

        // Estensione 3a: Approva menù con proposte di modifica
        em.approveMenu(srv, menu, "Aggiungere opzione senza glutine negli antipasti");

        assertTrue(srv.isMenuApproved());
        assertEquals(menu.getId(), srv.getMenuId());
        assertEquals("Aggiungere opzione senza glutine negli antipasti", srv.getProposedMenuModifications());
    }

    @Test
    @Order(4)
    void testTerminateEventWithNotesAndDocs() throws UseCaseLogicException {
        EventManager em = app.getEventManager();

        Event ev = em.createEvent("Conferenza Tech", "Lingotto", Date.valueOf("2026-09-10"), Date.valueOf("2026-09-10"));
        em.confirmEvent();

        // Estensione *a: Termina evento con note e documentazione
        em.terminateEvent("Evento riuscito perfettamente, nessun imprevisto.", "fattura_finale_2026.pdf");

        assertEquals("terminato", ev.getState());
        assertEquals("Evento riuscito perfettamente, nessun imprevisto.", ev.getNotes());
        assertEquals("fattura_finale_2026.pdf", ev.getDocumentation());
    }

    @Test
    @Order(5)
    void testCancelAndDeleteEventExceptions() throws UseCaseLogicException {
        EventManager em = app.getEventManager();

        Event ev = em.createEvent("Aperitivo Inaugurazione", "Piazza Castello", Date.valueOf("2026-10-05"), Date.valueOf("2026-10-05"));
        em.terminateEvent("Concluso", "doc.pdf");

        // Un evento già terminato NON può essere annullato né eliminato
        assertThrows(UseCaseLogicException.class, () -> em.cancelEvent());
        assertThrows(UseCaseLogicException.class, () -> em.deleteEvent(ev));

        // Un evento in lavorazione può essere eliminato regolarmente
        Event ev2 = em.createEvent("Evento Provvisorio", "Ufficio", Date.valueOf("2026-11-01"), Date.valueOf("2026-11-01"));
        assertTrue(em.deleteEvent(ev2));
        assertNull(Event.loadById(ev2.getId()));
    }
}