/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmris.json;

/**
 *
 * @author rhwood
 */
public final class JSON {

    /* JSON structure */
    public static final String TYPE = "type"; // NOI18N
    public static final String LIST = "list"; // NOI18N
    public static final String DATA = "data"; // NOI18N
    public static final String PING = "ping"; // NOI18N
    public static final String PONG = "pong"; // NOI18N
    public static final String GOODBYE = "goodbye"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    /* JSON common tokens */
    public static final String COMMENT = "comment"; // NOI18N
    public static final String USERNAME = "userName"; // NOI18N
    public static final String STATE = "state"; // NOI18N
    public static final String VALUE = "value"; // NOI18N
    public static final String ID = "id"; // NOI18N
    /* JSON error */
    public static final String ERROR = "error"; // NOI18N
    public static final String CODE = "code"; // NOI18N
    public static final String MESSAGE = "message"; // NOI18N
    /* JSON hello */
    public static final String HELLO = "hello"; // NOI18N
    public static final String JMRI = "JMRI"; // NOI18N
    public static final String HEARTBEAT = "heartbeat"; // NOI18N
    public static final String RAILROAD = "railroad"; // NOI18N
    /* JSON list types */
    public static final String CARS = "cars"; // NOI18N
    public static final String ENGINES = "engines"; // NOI18N
    public static final String LIGHTS = "lights"; // NOI18N
    public static final String LOCATIONS = "locations"; // NOI18N
    public static final String MEMORIES = "memories"; // NOI18N
    public static final String METADATA = "metadata"; // NOI18N
    public static final String PANELS = "panels"; // NOI18N
    public static final String ROSTER = "roster"; // NOI18N
    public static final String ROUTES = "routes"; // NOI18N
    public static final String SENSORS = "sensors"; // NOI18N
    public static final String SIGNAL_HEADS = "signalHeads"; // NOI18N
    public static final String SIGNAL_MASTS = "signalMasts"; // NOI18N
    public static final String TRAINS = "trains"; // NOI18N
    public static final String TURNOUTS = "turnouts"; // NOI18N
    /* JSON data types */
    public static final String CAR = "car"; // NOI18N
    public static final String ENGINE = "engine"; // NOI18N
    public static final String LIGHT = "light"; // NOI18N
    public static final String LOCATION = "location"; // NOI18N
    public static final String MEMORY = "memory"; // NOI18N
    public static final String OPERATIONS = "operations"; // NOI18N
    public static final String PANEL = "panel"; // NOI18N
    public static final String POWER = "power"; // NOI18N
    public static final String PROGRAMMER = "programmer"; // NOI18N
    public static final String ROUTE = "route"; // NOI18N
    public static final String SENSOR = "sensor"; // NOI18N
    public static final String SIGNAL_HEAD = "signalHead"; // NOI18N
    public static final String SIGNAL_MAST = "signalMast"; // NOI18N
    public static final String REPORTER = "reporter"; // NOI18N
    public static final String ROSTER_ENTRY = "rosterEntry"; // NOI18N
    public static final String THROTTLE = "throttle"; // NOI18N
    public static final String TRAIN = "train"; // NOI18N
    public static final String TURNOUT = "turnout"; // NOI18N
    /* JSON operations tokens */
    public static final String LENGTH = "length"; // NOI18N
    public static final String STATUS = "status"; // NOI18N
    public static final String WEIGHT = "weight"; // NOI18N
    public static final String LEAD_ENGINE = "leadEngine"; // NOI18N
    public static final String CABOOSE = "caboose"; // NOI18N
    public static final String TERMINATE = "terminate"; // NOI18N
    /* JSON panel tokens */
    public static final String CONTROL_PANEL = "Control Panel"; // NOI18N
    public static final String LAYOUT_PANEL = "Layout"; // NOI18N
    public static final String PANEL_PANEL = "Panel"; // NOI18N
    public static final String URL = "URL"; // NOI18N
    /* JSON programmer tokens */
    public static final String MODE = "mode"; // NOI18N
    public static final String NODE_CV = "CV"; // NOI18N
    public static final String OP = "mode"; // NOI18N
    public static final String READ = "read"; // NOI18N
    public static final String WRITE = "write"; // NOI18N
    /* JSON reporter tokens */
    public static final String REPORT = "report"; // NOI18N
    public static final String LAST_REPORT = "lastReport"; // NOI18N
    /* JSON roster and car/engine (operations) tokens */
    public static final String COLOR = "color"; // NOI18N
    public static final String LOAD = "load"; // NOI18N
    public static final String MODEL = "model"; // NOI18N
    public static final String ROAD = "road"; // NOI18N
    public static final String NUMBER = "number"; // NOI18N
    public static final String DESTINATION = "destination"; // NOI18N
    public static final String DESTINATION_TRACK = "destinationTrack"; // NOI18N
    public static final String LOCATION_TRACK = "locationTrack"; // NOI18N
    public static final String IS_LONG_ADDRESS = "isLongAddress"; // NOI18N
    public static final String MFG = "mfg"; // NOI18N
    public static final String MAX_SPD_PCT = "maxSpeedPct"; // NOI18N
    public static final String IMAGE_FILE_NAME = "imageFileName"; // NOI18N
    public static final String IMAGE_ICON_NAME = "imageFileName"; // NOI18N
    public static final String FUNCTION_KEYS = "functionKeys"; // NOI18N
    public static final String LABEL = "label"; // NOI18N
    public static final String LOCKABLE = "lockable"; // NOI18N
    /* JSON route (operations) tokens */
    public static final String DIRECTION = "trainDirection"; // NOI18N
    public static final String SEQUENCE = "sequenceId"; // NOI18N
    public static final String EXPECTED_ARRIVAL = "expectedArrivalTime"; // NOI18N
    public static final String EXPECTED_DEPARTURE = "expectedDepartureTime"; // NOI18N
    public static final String DEPARTURE_TIME = "departureTime"; // NOI18N
    public static final String DEPARTURE_LOCATION = "trainDepartsName"; // NOI18N
    public static final String TERMINATES_LOCATION = "trainTerminatesName"; // NOI18N
    public static final String DESCRIPTION = "description"; // NOI18N
    public static final String ROUTE_ID = "routeId"; // NOI18N
    /* JSON signalling tokens */
    public static final String APPEARANCE = "appearance"; // NOI18N
    public static final String APPEARANCE_NAME = "appearanceName"; // NOI18N
    public static final String ASPECT = "aspect"; // NOI18N
    public static final String ASPECT_DARK = "Dark"; // NOI18N
    public static final String ASPECT_HELD = "Held"; // NOI18N
    public static final String ASPECT_UNKNOWN = "Unknown"; // NOI18N
    public static final String HELD = "held"; // NOI18N
    public static final String LIT = "lit"; // NOI18N
    /* JSON throttle tokens */
    public static final String ADDRESS = "address"; // NOI18N
    public static final String FORWARD = "forward"; // NOI18N
    public static final String RELEASE = "release"; // NOI18N
    public static final String ESTOP = "eStop"; // NOI18N
    public static final String IDLE = "idle"; // NOI18N
    public static final String SPEED = "speed"; // NOI18N
    public static final String SSM = "SSM"; // NOI18N
    public static final String F = "F"; // NOI18N
    /* JSON Sensor and Turnout Tokens */
    public static final String INVERTED = "inverted"; // NOI18N
}
