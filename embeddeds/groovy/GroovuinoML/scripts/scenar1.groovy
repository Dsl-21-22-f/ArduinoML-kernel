sensor "button" onPin 9
actuator "buzzer" pin 11
actuator "led" pin 10

state "on" means "buzzer" becomes "high" and "led" becomes "high"
state "off" means "buzzer" becomes low and "led" becomes "low"

initial "off"

from "on" to "off" when "button" becomes "up"
from off to on when button becomes down

export "Switch!"