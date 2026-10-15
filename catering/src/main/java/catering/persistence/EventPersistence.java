package catering.persistence;

import catering.businesslogic.event.Event;
import catering.businesslogic.event.EventReceiver;
import catering.businesslogic.event.Service;
import catering.businesslogic.event.StaffRequest;

public class EventPersistence implements EventReceiver {

    @Override
    public void updateEventCreated(Event e) {
        e.saveNewEvent();
    }

    @Override
    public void updateEventUpdated(Event e) {
        e.updateEvent();
        if (e.isRecurring() && e.getInstances() != null) {
            for (var inst : e.getInstances()) {
                if (inst.getId() == 0) {
                    inst.setEventId(e.getId());
                    inst.saveNewInstance();
                } else {
                    inst.updateInstance();
                }
            }
        }
    }

    @Override
    public void updateEventDeleted(Event e) {
        e.deleteEvent();
    }

    @Override
    public void updateServiceCreated(Event e, Service s) {
        s.setEventId(e.getId());
        s.saveNewService();
    }

    @Override
    public void updateServiceUpdated(Service s) {
        s.updateService();
        if (s.isMenuApproved()) {
            s.saveMenuProposal();
        }
    }

    @Override
    public void updateStaffRequested(Service s, StaffRequest sr) {
        sr.setServiceId(s.getId());
        sr.saveNewStaffRequest();
    }
}