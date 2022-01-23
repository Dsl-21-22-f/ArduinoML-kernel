sensor "switch" onPin 9
actuator "led" pin 10

state "on" means "led" becomes "high"
state "off" means "led" becomes "low"

initial "off"

from "on" to "off" when "switch" becomes "pushed"
from off to on when "switch" becomes pushed

export "Switch!"