package catering.businesslogic.event;

public interface EventReceiver {
    void updateEventCreated(Event e);
    void updateEventUpdated(Event e);
    void updateEventDeleted(Event e);
    void updateServiceCreated(Event e, Service s);
    void updateServiceUpdated(Service s);
    void updateStaffRequested(Service s, StaffRequest sr);
}