sensor "button" onPin 9
sensor "button2" pin 12
actuator "buzzer" pin 11
actuator "led" pin 10

state "on" means "buzzer" becomes "high" and "led" becomes "high"
state "off" means "buzzer" becomes low and "led" becomes "low"

initial "off"

from off to on when button becomes down
from off to on when "button2" becomes "down"
from "on" to "off" when "button" becomes "up" and button2 becomes "up"


export "Switch!"