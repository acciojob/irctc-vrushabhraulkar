package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int noOfAvailableTickets = train.getNoOfSeats() - train.getBookedTickets().size();
        if (noOfAvailableTickets < bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        //Throw exception Invalid stations
        String routeStr = train.getRoute();
        String[] routeStrArr = routeStr.split(",");

        boolean doesFromStationExists = false;

        boolean doesToStationExists = false;

        int indexOfFromStation = -1;

        int indexOfToStation = -1;

        String fromStation = bookTicketEntryDto.getFromStation().toString();

        String toStation = bookTicketEntryDto.getToStation().toString();


        for (int i = 0;i<routeStrArr.length;i++){
            String currStation = routeStrArr[i];
            if (currStation.equals(fromStation)){
                doesFromStationExists = true;
                indexOfFromStation = i;
            }

            if (currStation.equals(toStation)){
                doesToStationExists = true;
                indexOfToStation = i;
            }
        }

        if (!doesFromStationExists || !doesToStationExists || indexOfFromStation>indexOfToStation){
            throw new Exception("Invalid stations");
        }



        int totalFare = (indexOfToStation - indexOfFromStation) * 300;
        Ticket ticket = new Ticket();
        List<Passenger> passengerList = new ArrayList<>();
//        passengerList.add(passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get());
        for (int passengerId : bookTicketEntryDto.getPassengerIds()){
            passengerList.add(passengerRepository.findById(passengerId).get());
        }
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setTotalFare(totalFare);


        ticket = ticketRepository.save(ticket);

        List<Ticket> bookedTicketsTrain = train.getBookedTickets();
        bookedTicketsTrain.add(ticket);

        trainRepository.save(train);


        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        List<Ticket> bookedTicketsPassenger = passenger.getBookedTickets();
        bookedTicketsPassenger.add(ticket);
        passengerRepository.save(passenger);


        return ticket.getTicketId();

    }
}
