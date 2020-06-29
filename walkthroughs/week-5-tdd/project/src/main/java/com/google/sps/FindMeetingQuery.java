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
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int requestDuration = (int) request.getDuration();

    //if there are no events and the request duration is less than a day, return the whole day
    if (events.isEmpty() && requestDuration <= TimeRange.WHOLE_DAY.duration()){
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    //if the requested meeting is longer than a day, return an empty list since this is not possible
    if (requestDuration > TimeRange.WHOLE_DAY.duration()) {
        return Collections.emptyList();
    }

    // A list without duplicates of all busy times in a day. The list is ordered chronologically based on the starting times of each event. 
    ArrayList<TimeRange> busyTimes = new ArrayList<TimeRange>();

    //adding the beginning and end of the day as endpoints (your meeting can't be before the beginning of the day or after the end of the day)
    busyTimes.add(TimeRange.fromStartDuration(0, 0));
    busyTimes.add(TimeRange.fromStartDuration(60*24,0));

    for (Event event : events){
        //create a timerange for the event we're evaluating
        TimeRange newEvent = TimeRange.fromStartEnd(event.getWhen().start(), event.getWhen().duration()+event.getWhen().start(), false);
        int newEventStart = newEvent.start();
        int newEventEnd = newEvent.end();

        //if none of the attendees in the request are attending this event, don't add it to BusyTimes because it's not relevant
        if (Collections.disjoint(event.getAttendees(), request.getAttendees())){
            continue;
        }

        for (int i = 0; i < busyTimes.size()-1; i++) {
            int currentBusyStart = busyTimes.get(i).start();
            int currentBusyEnd = busyTimes.get(i).end();
            int nextBusyStart = busyTimes.get(i+1).start();
            int nextBusyEnd = busyTimes.get(i+1).end();
            int adjustedNewStart;
            int adjustedNewEnd;

            //if the current busy time contains the new event, or they are equal, do not add the new event to the arraylist as it already exists
            // current busy block: #######
            // new event:           ####
            if (busyTimes.get(i).contains(newEvent) || busyTimes.get(i).equals(newEvent)){
                break; 
            }
            //if the new event starts during the current busy time and ends before the next busy time
            // busy blocks: #######     ####
            // new event:        ####
            else if (TimeRange.contains(busyTimes.get(i), newEventStart) && newEventEnd <= nextBusyStart) {
                adjustedNewStart = currentBusyEnd;
                adjustedNewEnd = newEventEnd;
            }
            //if the new event's start is during the current busy time and it's end is in the next busy time, fill in the time between the busy times
            // busy blocks: #######     ######
            // new event:        #########
            else if (TimeRange.contains(busyTimes.get(i), newEventStart) && TimeRange.contains(busyTimes.get(i+1), newEventEnd)) {
                adjustedNewStart = currentBusyEnd;
                adjustedNewEnd = nextBusyStart;
            }
            //if the new event is strictly between the current and next busy times, add it as is
            // busy blocks: #######         ######
            // new event:             ###
            else if (newEventStart >= currentBusyEnd && newEventEnd <= nextBusyStart) {
                adjustedNewStart = newEventStart;
                adjustedNewEnd = newEventEnd;
            }
            //if it's start time is after the current busy block's end time and it's end time is during the next busy time
            // busy blocks: #######      ######
            // new event:             #####            
            else if (newEventStart >= currentBusyEnd && newEventStart < nextBusyStart && TimeRange.contains(busyTimes.get(i+1), newEventEnd)) {
                adjustedNewStart = newEventStart;
                adjustedNewEnd = nextBusyStart;
            }
            //the new event does not start during the current busy block or before the next busy block, so move on to next busy block
            else {
                continue;
            }

            //add the new busy time with the adjusted start and duration
            busyTimes.add(TimeRange.fromStartEnd(adjustedNewStart, adjustedNewEnd, false));
            //sort the list in chronological order by event start time
            Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
            break;
        }
    }

    //list of meeting times to return as possible solutions
    ArrayList<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    for (int i = 0; i < busyTimes.size()-1; i++){
        int currentBusyEnd =  busyTimes.get(i).end();
        int nextBusyStart = busyTimes.get(i+1).start();
        int timeSlot = nextBusyStart - currentBusyEnd;

        if (timeSlot >= requestDuration){
            meetingTimes.add(TimeRange.fromStartDuration(currentBusyEnd, timeSlot));
        }
    }
    
    return meetingTimes;
  }
}


