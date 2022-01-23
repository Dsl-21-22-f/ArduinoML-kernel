sensor "button" onPin 8
actuator "buzzer" pin 12
actuator "led" pin 11


state "off" means "buzzer" becomes low and "led" becomes "low"
state "onLed" means "led" becomes "high"
state "onBuzz" means "buzzer" becomes "high"
state "offLed" means "buzzer" becomes "low" and "led" becomes "low"

initial "off"

from off to onLed when button becomes pushed
from "onLed" to "onBuzz" when "button" becomes pushed
from "onBuzz" to "offLed" when "button" becomes pushed
from "offLed" to "off" when "button" becomes pushed

export "Switch!"