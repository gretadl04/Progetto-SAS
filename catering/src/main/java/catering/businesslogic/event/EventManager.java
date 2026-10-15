package catering.businesslogic.event;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;

public class EventManager {

    private Event currentEvent;
    private Service currentService;
    private ArrayList<EventReceiver> eventReceivers;

    public EventManager() {
        this.eventReceivers = new ArrayList<>();
    }

    private void checkOrganizerRole() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (user == null || !user.isOrganizer()) {
            throw new UseCaseLogicException("Operazione consentita solo a un Organizzatore.");
        }
    }

    // Passo 1: Creazione nuovo evento
    public Event createEvent(String name, String location, Date dateStart, Date dateEnd) throws UseCaseLogicException {
        checkOrganizerRole();
        if (dateStart != null && dateEnd != null && dateEnd.before(dateStart)) {
            throw new UseCaseLogicException("La data di fine non può precedere la data di inizio.");
        }
        Event e = new Event(name);
        e.setLocation(location);
        e.setDateStart(dateStart);
        e.setDateEnd(dateEnd);
        e.setState("in lavorazione");

        this.currentEvent = e;
        this.currentService = null;
        notifyEventCreated(e);
        return e;
    }

    // Metodo compatibile col template base se presente
    public Event createEvent(String name, Date dateStart, Date dateEnd, User chef) throws UseCaseLogicException {
        Event e = createEvent(name, "Sede principale", dateStart, dateEnd);
        if (chef != null) {
            assignChef(chef);
        }
        return e;
    }

    // Estensione 1a: Selezione di un evento esistente
    public Event selectEvent(Event e) throws UseCaseLogicException {
        checkOrganizerRole();
        if (e == null) {
            throw new UseCaseLogicException("Evento non valido.");
        }
        this.currentEvent = e;
        return this.currentEvent;
    }

    // Passo 3: Assegnazione Chef all'evento
    public void assignChef(User chef) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento selezionato.");
        }
        if (chef == null || !chef.isChef()) {
            throw new UseCaseLogicException("L'utente indicato non ha il ruolo di Chef.");
        }
        currentEvent.setChef(chef);
        notifyEventUpdated(currentEvent);
    }

    // Passo 5: Definizione di un servizio
    public Service createService(String name, Date date, Time timeStart, Time timeEnd, String location) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento in corso di definizione.");
        }
        Service s = new Service(name);
        s.setEventId(currentEvent.getId());
        s.setDate(date != null ? date : currentEvent.getDateStart());
        s.setTimeStart(timeStart);
        s.setTimeEnd(timeEnd);
        s.setLocation(location != null ? location : currentEvent.getLocation());

        currentEvent.addService(s);
        this.currentService = s;
        notifyServiceCreated(currentEvent, s);
        return s;
    }

    // Metodo rapido per aggiungere un servizio indicando solo la tipologia (come da SSD/DSD)
    public Service addService(String serviceType) throws UseCaseLogicException {
        return createService(serviceType, currentEvent != null ? currentEvent.getDateStart() : null,
                Time.valueOf("12:00:00"), Time.valueOf("15:00:00"),
                currentEvent != null ? currentEvent.getLocation() : null);
    }

    // Passo 7: Definisce turno e richiede il personale specificando ruolo e quantità
    public StaffRequest addShiftAndRequestStaff(Date date, Time startTime, Time endTime, String role, int quantity) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null || currentService == null) {
            throw new UseCaseLogicException("Devi prima selezionare un evento e un servizio.");
        }
        if (quantity <= 0 || role == null || role.trim().isEmpty()) {
            throw new UseCaseLogicException("Ruolo e quantità (>0) sono obbligatori.");
        }

        Shift shift = CatERing.getInstance().getShiftManager().createShift(
        date != null ? date : currentService.getDate(),
        startTime,
        endTime
        );

        StaffRequest sr = new StaffRequest(currentService.getId(), shift, role, quantity);
        currentService.addStaffRequest(sr);
        notifyStaffRequested(currentService, sr);
        return sr;
    }

    // Passo 9: Conferma dell'evento
    public void confirmEvent() throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento in corso.");
        }
        currentEvent.setState("programmato");
        notifyEventUpdated(currentEvent);
    }

    // Estensione 1b: Definizione ricorrenza
    public void defineRecurrence(String frequency, Date endRecurrence) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento in corso.");
        }
        if (endRecurrence == null || (currentEvent.getDateStart() != null && endRecurrence.before(currentEvent.getDateStart()))) {
            throw new UseCaseLogicException("Data di fine ricorrenza non valida.");
        }
        currentEvent.generateRecurrenceInstances(frequency, endRecurrence);
        notifyEventUpdated(currentEvent);
    }

    // Estensione 1b.1: Modifica / Annullamento di una singola istanza o di tutte le istanze della ricorrenza
    public void modifyRecurrenceEvents(EventInstance targetInstance, boolean applyToAll, String newLocation, String newState) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null || !currentEvent.isRecurring()) {
            throw new UseCaseLogicException("L'evento corrente non è un evento ricorrente.");
        }
        if (applyToAll) {
            if (newLocation != null) currentEvent.setLocation(newLocation);
            if (newState != null) currentEvent.setState(newState);
            for (EventInstance inst : currentEvent.getInstances()) {
                if (newLocation != null) inst.setCustomLocation(newLocation);
                if (newState != null) inst.setState(newState);
                inst.updateInstance();
            }
        } else {
            if (targetInstance == null || !currentEvent.getInstances().contains(targetInstance)) {
                throw new UseCaseLogicException("Istanza ricorrente non valida.");
            }
            if (newLocation != null) targetInstance.setCustomLocation(newLocation);
            if (newState != null) targetInstance.setState(newState);
            targetInstance.setModified(true);
            targetInstance.updateInstance();
        }
        notifyEventUpdated(currentEvent);
    }

    // Estensione 3a: Approvazione menù proposto dallo Chef con eventuali proposte di modifica
    public void approveMenu(Service service, Menu menu, String proposedModifications) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null || service == null || !currentEvent.containsService(service)) {
            throw new UseCaseLogicException("Servizio non appartenente all'evento corrente.");
        }
        if (menu == null) {
            throw new UseCaseLogicException("Menù non valido.");
        }
        service.approveMenu(menu, proposedModifications);
        notifyServiceUpdated(service);
    }

    // Estensione *a: Terminare un evento aggiungendo note e documentazione
    public void terminateEvent(String notes, String documentation) throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento selezionato.");
        }
        if ("annullato".equalsIgnoreCase(currentEvent.getState())) {
            throw new UseCaseLogicException("Impossibile terminare un evento già annullato.");
        }
        currentEvent.setState("terminato");
        currentEvent.setNotes(notes);
        currentEvent.setDocumentation(documentation);
        notifyEventUpdated(currentEvent);
    }

    // Estensione *b: Annullare un evento
    public void cancelEvent() throws UseCaseLogicException {
        checkOrganizerRole();
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento selezionato.");
        }
        if ("terminato".equalsIgnoreCase(currentEvent.getState())) {
            throw new UseCaseLogicException("Non è possibile annullare un evento già terminato.");
        }
        currentEvent.setState("annullato");
        notifyEventUpdated(currentEvent);
    }

    // Estensione *b: Eliminare un evento (a certe condizioni)
    public boolean deleteEvent(Event e) throws UseCaseLogicException {
        checkOrganizerRole();
        if (e == null) {
            throw new UseCaseLogicException("Evento non valido.");
        }
        if ("in corso".equalsIgnoreCase(e.getState()) || "terminato".equalsIgnoreCase(e.getState())) {
            throw new UseCaseLogicException("Impossibile eliminare un evento in corso o già terminato.");
        }
        notifyEventDeleted(e);
        if (currentEvent != null && currentEvent.getId() == e.getId()) {
            currentEvent = null;
            currentService = null;
        }
        return true;
    }

    public ArrayList<Event> getAllEvents() {
        return Event.loadAllEvents();
    }

    public Event getCurrentEvent() { return currentEvent; }
    public Service getCurrentService() { return currentService; }
    public void setCurrentService(Service currentService) { this.currentService = currentService; }

    // Observer Pattern
    public void addEventReceiver(EventReceiver er) {
        if (!eventReceivers.contains(er)) eventReceivers.add(er);
    }

    public void removeEventReceiver(EventReceiver er) {
        eventReceivers.remove(er);
    }

    private void notifyEventCreated(Event e) {
        for (EventReceiver er : eventReceivers) er.updateEventCreated(e);
    }

    private void notifyEventUpdated(Event e) {
        for (EventReceiver er : eventReceivers) er.updateEventUpdated(e);
    }

    private void notifyEventDeleted(Event e) {
        for (EventReceiver er : eventReceivers) er.updateEventDeleted(e);
    }

    private void notifyServiceCreated(Event e, Service s) {
        for (EventReceiver er : eventReceivers) er.updateServiceCreated(e, s);
    }

    private void notifyServiceUpdated(Service s) {
        for (EventReceiver er : eventReceivers) er.updateServiceUpdated(s);
    }

    private void notifyStaffRequested(Service s, StaffRequest sr) {
        for (EventReceiver er : eventReceivers) er.updateStaffRequested(s, sr);
    }
}