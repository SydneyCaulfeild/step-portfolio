// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int requestDuration = (int) request.getDuration();

    // If there are no events and the request duration is less than a day, return the whole day
    if (events.isEmpty() && requestDuration <= TimeRange.WHOLE_DAY.duration()){
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // If the requested meeting is longer than a day, return an empty list since this is not possible
    if (requestDuration > TimeRange.WHOLE_DAY.duration()) {
        return Collections.emptyList();
    }

    // Create a map to hold an ordered pair without duplicates of all events in the day. The set is ordered chronologically based on the starting times of each event. The Boolean is true if there are optional attendees attending this event, otherwise it's false.
    TreeMap<TimeRange, Boolean> busyTimes = new TreeMap<>(new Comparator<TimeRange>() {
         @Override
            public int compare(TimeRange s1, TimeRange s2) {
                return TimeRange.ORDER_BY_START.compare(s1, s2);
            }
    });

    // Add the beginning and end of the day as endpoints (the meeting can't be before the beginning of the day or after the end of the day)
    busyTimes.put(TimeRange.fromStartDuration(0, 0), new Boolean(false));
    busyTimes.put(TimeRange.fromStartDuration(60*24,0), new Boolean(false));

    for (Event event : events){        
        // Create a timerange for the event that is currently being evaluated
        TimeRange newEvent = TimeRange.fromStartEnd(event.getWhen().start(), event.getWhen().duration()+event.getWhen().start(), false);
        int newEventStart = newEvent.start();
        int newEventEnd = newEvent.end();

        // If none of the attendees in the request are attending this event, don't add it to BusyTimes because it's not relevant
        if (Collections.disjoint(event.getAttendees(), request.getAttendees()) && Collections.disjoint(event.getAttendees(), request.getOptionalAttendees())){
            continue;
        }

        for (Map.Entry<TimeRange, Boolean> busyTime : busyTimes.entrySet()) {
            // Need to stop at the second last element because comparisons are done between the current and next busy time
            if (busyTime.getKey() == busyTimes.lastKey()) {
                break;
            }

            Map.Entry<TimeRange, Boolean> nextBusyTime = busyTimes.higherEntry(busyTime.getKey());
            int currentBusyStart = busyTime.getKey().start();
            int currentBusyEnd = busyTime.getKey().end();
            int nextBusyStart = nextBusyTime.getKey().start();
            int nextBusyEnd = nextBusyTime.getKey().end();
            int adjustedNewStart;
            int adjustedNewEnd;

            // If the current busy time contains the new event, or they are equal, do not add the new event to the arraylist as it already exists
            // current busy block: #######
            // new event:           ####
            if (busyTime.getKey().contains(newEvent) || busyTime.getKey().equals(newEvent)){
                break; 
            }
            // If the new event starts during the current busy time and ends before the next busy time
            // busy blocks: #######     ####
            // new event:        ####
            else if (TimeRange.contains(busyTime.getKey(), newEventStart) && newEventEnd <= nextBusyStart) {
                adjustedNewStart = currentBusyEnd;
                adjustedNewEnd = newEventEnd;
            }
            // If the new event's start is during the current busy time and it's end is in the next busy time, fill in the time between the busy times
            // busy blocks: #######     ######
            // new event:        #########
            else if (TimeRange.contains(busyTime.getKey(), newEventStart) && TimeRange.contains(nextBusyTime.getKey(), newEventEnd)) {
                adjustedNewStart = currentBusyEnd;
                adjustedNewEnd = nextBusyStart;
            }
            // If the new event is strictly between the current and next busy times, add it as is
            // busy blocks: #######         ######
            // new event:             ###
            else if (newEventStart >= currentBusyEnd && newEventEnd <= nextBusyStart) {
                adjustedNewStart = newEventStart;
                adjustedNewEnd = newEventEnd;
            }
            // If it's start time is after the current busy block's end time and it's end time is during the next busy time
            // busy blocks: #######      ######
            // new event:             #####            
            else if (newEventStart >= currentBusyEnd && newEventStart < nextBusyStart && TimeRange.contains(nextBusyTime.getKey(), newEventEnd)) {
                adjustedNewStart = newEventStart;
                adjustedNewEnd = nextBusyStart;
            }
            // The new event does not start during the current busy block or before the next busy block, so move on to next busy block
            else {
                continue;
            }

            // If the event doesn't have optional attendees from the request, add a busy time and indicate no optional attendees 
            if (Collections.disjoint(event.getAttendees(), request.getOptionalAttendees())){
                busyTimes.put(TimeRange.fromStartEnd(adjustedNewStart, adjustedNewEnd, false), new Boolean (false));

            }
            else {
                // Add the new busy time with the adjusted start and duration, and indicate there are optional attendees
                busyTimes.put(TimeRange.fromStartEnd(adjustedNewStart, adjustedNewEnd, false), new Boolean (true));                
            }
            break;
        }
    }

    // Lists of meeting times to return as possible solutions
    ArrayList<TimeRange> meetingTimesAllAttendees = new ArrayList<TimeRange>();
    ArrayList<TimeRange> meetingTimesOnlyMandatory = new ArrayList<TimeRange>();

    for (Map.Entry<TimeRange, Boolean> busyTime : busyTimes.entrySet()){
        // Need to stop at the second last element
        if (busyTime.getKey() == busyTimes.lastKey()) {
            break;
        }
        Map.Entry<TimeRange, Boolean> nextBusyTime = busyTimes.higherEntry(busyTime.getKey());
        int currentBusyEnd =  busyTime.getKey().end();
        int nextBusyStart = nextBusyTime.getKey().start();
        int timeSlot = nextBusyStart - currentBusyEnd;

        if (timeSlot >= requestDuration) {
            // If there are optional attendees, add this time slot to the meeting times with all attendees
            if (busyTime.getValue() == true){
                meetingTimesAllAttendees.add(TimeRange.fromStartDuration(currentBusyEnd, timeSlot));
            }
            // If the busy time did not have optional attendees, add this time slot to the meeting times list for only mandatory attendees
            else {
                meetingTimesOnlyMandatory.add(TimeRange.fromStartDuration(currentBusyEnd, timeSlot));
            }
        }
        // If the timeslot isn't big enough for the requested meeting's duration, there are so far no meetings with only mandatory attendees and there are mandatory
        // attendees in the request, compare the current busy time with the next busy time that ONLY has mandatory attendees to see if you can find a meeting without 
        // optional attendees
        else {
            if (meetingTimesOnlyMandatory.isEmpty() && (!request.getAttendees().isEmpty())) {
                while(nextBusyTime.getValue() == true){
                    nextBusyTime = busyTimes.higherEntry(nextBusyTime.getKey());
                }
                nextBusyStart = nextBusyTime.getKey().start();
                timeSlot = nextBusyStart - currentBusyEnd;
                if (timeSlot >= requestDuration) {
                    meetingTimesOnlyMandatory.add(TimeRange.fromStartDuration(currentBusyEnd, timeSlot));
                }
            }
        }
    }

    // If there are possible solutions with all the attendees, return them. Otherwise, return those only for mandatory attendees
    if (meetingTimesAllAttendees.size() >= 1) {
        return meetingTimesAllAttendees;
    }
    else {
        return meetingTimesOnlyMandatory;
    }
  }
}


