import guitarpro as gp

NUM_FRETS = 7  # The First fret (controller's LEDs) is save for string indication
BEND_EFFECT_EVENT_TIME = 4
HAMMER_EFFECT_EVENT_TIME = 4
SLIDE_EFFECT_EVENT_TIME = 8
EFFECT_VID_REPEATS = 3


class GuitarProControllerParser:
    """
    This class purpose is to create a second list from a GuitarPro file. Each event can later be translated to the Guitar Controller's dot string
    The guitarPro file must be from of the following extensions: .gp3 .gp4 or .gp5 .
    """

    def __init__(self, path):
        self.gp = gp.parse(path)
        self.events = []
        self.measures_start_event_indices = []
        self.tracks = []

    def fetch_tracks_names(self):
        """
        Retrieve a list of tuples of all the Guitar pro file existing tracks and their index number in the file
        Example: [(trackIndex, trackName1), (trackNum2, trackName2), ... ] .
        :return:  the list
        """
        return list(map(lambda x: str(x.name), self.gp.tracks)) 


    def get_measure_start_event_indices(self):
        return self.get_measure_start_event_indices()

    def get_second_in_index(self, index):
        """

        :param index:
        :return:
        """
        return self.events[index]

    def find_succ_note_for_effect(self, note, succ_beat):
        """

        :param note:
        :param succ_beat:
        :return:
        """
        for succ_note in succ_beat.notes:
            if succ_note.string == note.string:  # this is where the hammer/pull is going to be
                return succ_note
        return None

    def add_hammer_effect_event(self, note, succ_beat):
        """
        add events for hammer-on or pull-off envet in the note
        a legal hammer event is when the successive beat has a note on the same string with a greater value
        :param note: the note to create the event to
        :param succ_beat: the
        :return:
        """
        succ_note = self.find_succ_note_for_effect(note, succ_beat)
        if succ_note is not None:
            for repeat in range(EFFECT_VID_REPEATS):
                self.add_dot(note.value, note.string, "blue")
                self.add_dot(succ_note.value, succ_note.string, "green")
                self.events.append({"event_type": "vid", "time": HAMMER_EFFECT_EVENT_TIME})
                self.add_dot(note.value, note.string, "blue")
                self.events.append({"event_type": "vid", "time": HAMMER_EFFECT_EVENT_TIME})

    def add_slide_effect_event(self, note, succ_beat):
        """
        add events for hammer-on or pull-off envet in the note
        a legal slide event is when the successive beat has a note on the same string with a greater value
        :param note: the note to create the event to
        :param succ_beat: the
        :return:
        """
        succ_note = self.find_succ_note_for_effect(note, succ_beat)
        if succ_note is not None:
            slide_direction = -1 if succ_note.value < note.value else 1
            for value in range(note.value, succ_note.value, slide_direction):
                self.add_dot(note.value, note.string, "blue")
                self.add_dot(succ_note.value, succ_note.string, "green")
                self.events.append({"event_type": "dot", "fret": value, "guitar_string": note.string, "color": "green"})
                self.events.append({"event_type": "vid", "time": SLIDE_EFFECT_EVENT_TIME})

    def add_bend_effect_event(self, note):
        """
        add events for hammer-on or pull-off envet in the note
        a legal slide event is when the successive beat has a note on the same string with a greater value
        :param note: the note to create the event to
        :param succ_beat: the
        :return:
        """
        print(note)
        bend_direction = -1 if 3 < note.string else 1  # The direction to show the effect in bend depends on the note string
        bend_strength = int(note.effect.bend.points[-1].value)
        if bend_strength > 6:  # Checked with a guitar and a strength bigger than 6 is 2 tones band (impossible to do in real life)
            pass
        bend_strength_controller = int((bend_strength / 4) +1)  # Guitar Pro bend strength is between 1-12 (This is a downsample to have a value between 1-3)

        for limit in range(note.string, note.string + bend_strength_controller * bend_direction + bend_direction, bend_direction):
            print("limit" + str(limit))
            self.add_dot(note.value, note.string, "blue")
            for bend_indication_string in range(note.string, limit + bend_direction, bend_direction):
                print("indication string" + str(bend_indication_string))
                self.events.append({"event_type": "dot", "fret": 0, "guitar_string": bend_indication_string, "color": "purple"})
            self.events.append({"event_type": "vid", "time": BEND_EFFECT_EVENT_TIME})
        pass

    @staticmethod
    def is_note_has_slide(note):
        return len(note.effect.slides) and note.effect.slides[0] == gp.SlideType.shiftSlideTo

    def add_effect_events(self, beat, succ_beat):
        """
        This function adds events of the notes in the provided beat to be shown on the controller.
        We assumed that there is one effect on each beat (no two hammer-ons no two slides etc.)
        Because none is really a guitar hero. Except Guthrie Govan.
        :param beat: the beat to find the effect to
        :param succ_beat: the successive beat that comes a
        :return:
        """
        if succ_beat is None:  # we always provide the next beat
            pass
        for note in beat.notes:
            if note.type == gp.NoteType.normal:
                if self.is_note_has_slide(note):
                    self.add_slide_effect_event(note, succ_beat)
                    break
                if note.effect.hammer:
                    self.add_hammer_effect_event(note, succ_beat)
                    break
                if note.effect.bend:
                    self.add_bend_effect_event(note)
                    break

    def add_dot(self, fret, guitar_string, color):
        indication_color = "red" if fret > NUM_FRETS else "purple"
        self.events.append({"event_type": "dot", "fret": fret, "guitar_string": guitar_string, "color": color})
        self.events.append({"event_type": "dot", "fret": 0, "guitar_string": guitar_string, "color": indication_color})  # this is an event to indicate witch string to play

    def add_notes_events(self, beat):
        """
        This function adds events of the notes in the provided beat to be shown on the controller
        :param beat:
        :return:
        """
        note_appended = False
        for note in beat.notes:
            if note.type == gp.NoteType.normal:
                self.add_dot(note.value, note.string, "blue")
                note_appended = True
        if note_appended:
            self.events.append({"event_type": "hold", "time": beat.duration.value})  # Handle tuplets? maybe in the next project (:

    def parse_to_seconds(self, trackNumber):
        """
        parses a controller string for the given track number.

        :return: the number of events that was are ready to be fetched
        """
        track = self.gp.tracks[trackNumber]  #
        for measure in track.measures:
            self.measures_start_event_indices.append(len(self.events))  # that how we follow a measure's iterator index in the controller
            beats = measure.voices[0].beats  # we assume that there are no voices on the guitar pro track (the voices feature is unused on GuitarPro)
            for beat, succ_beat in zip(beats[0::], beats[1::]):
                self.add_effect_events(beat, succ_beat)
                self.add_notes_events(beat)

        self.events.append({"event_type": "eom"})
        return len(self.events)


