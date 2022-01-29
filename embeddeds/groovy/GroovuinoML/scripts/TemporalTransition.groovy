sensor "button" onPin 8
actuator "led" pin 11

state "on" means "led" becomes "high"
state "off" means "led" becomes low

from "on" to "off" after 800 ms
from "off" to "on" when button becomes "pushed"

export "Switch!"

