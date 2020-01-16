import guitarpro
from guitarpro import gp5


class GuitarProControllerParser:
    """
    This class purpose is to create a second list from a GuitarPro file. Each event can later be translated to the Guitar Controller's dot string
    The guitarPro file must be from of the following extensions: .gp3 .gp4 or .gp5 .
    """

    gp: gp5
    track: guitarpro.gp5.gp.Track

    def __init__(self, path):
        self.gp = guitarpro.parse(path)
        self.events = []
        self.measures_start_event_indices = []

    def fetch_tracks_names(self):
        """
        Retrieve a list of tuples of all the Guitar pro file existing tracks and their index number in the file
        Example: [(trackIndex, trackName1), (trackNum2, trackName2), ... ] .
        :return:  the list
        """
        return map(lambda x: x.name, self.gp.tracks)

    def get_measure_start_event_indices(self):
        return self.get_measure_start_event_indices()

    def get_second_in_index(self, index):
        """

        :param index:
        :return:
        """
        return self.events[index]

    def add_effect_events(self, note):
        pass

    def parse_to_seconds(self, trackNumber):
        """
        parses a controller string for the given track number.

        :return: the number of events that was are ready to be fetched
        """
        track = self.gp.tracks[trackNumber]  #
        for m in track.measures:
            voice = m.voices[0]  # we assume that there are no voices on the guitar pro track (the voices feature is unused on GuitarPro)
            self.measures_start_event_indices.append(len(self.events))
            for beat in voice.beats:
                # create dots
                for note in beat.notes:
                    # check effects
                    self.events.append({"event_type": "dot", "fret": note.value, "guitar_string": note.string, "color": "blue"})
                    self.events.append({"event_type": "dot", "fret": 0, "guitar_string": note.string, "color": "purple"})  # this is an event to indicate witch string to play
                self.events.append({"event_type": "hold", "time": beat.duration.value})  # Handle tuplets? maybe in the next project (:
        self.events.append({"event_type": "eom"})
        return len(self.events)


