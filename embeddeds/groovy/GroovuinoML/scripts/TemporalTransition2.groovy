sensor "button" onPin 8
actuator "led" pin 11
actuator "del" pin 12

state "onLed" means "led" becomes "high" and "del" becomes "high"
state "offLed" means "led" becomes low and "del" becomes "low"
state "onDel" means "del" becomes "high" and "led" becomes low

from "onLed" to "offLed" after 2000
from "onLed" to "offLed" before 2000 and button becomes pushed
from "offLed" to "onLed" when button becomes "pushed"
from "onDel" to "onLed" after 2000 
from "onDel" to "onLed" before 2000 and button becomes pushed
export "Switch!"

