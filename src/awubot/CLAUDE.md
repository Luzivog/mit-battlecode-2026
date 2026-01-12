Pathfinder.makeMove(target) and it will move towards the target if move cooldown is ready
don't check move cooldown, just call it
also, don't use rc.move or rc.moveForwards or rc.turn
use Pathfinder.makeMove(dir) and Pathfinder.turn(dir)
because it does auto cell tracking when you change the vision range


also, use the custom made interfaces in Util for better bytecode usage if needed. if something is missing there, use the normal rc methods and Java methods.

whatever you do, do it the most bytecode efficient way possible

always remember to check that you have all your imports in each file so that no 
compuile errors are caused

never edit babyrat.java