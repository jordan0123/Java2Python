10 DIM number AS INTEGER
20 LET number = 50 + 50 - 100
30 IF number < 100 THEN GOTO 50
40 GOTO 80
50 PRINT number
60 LET number = number + 1
70 GOTO 30
80 END