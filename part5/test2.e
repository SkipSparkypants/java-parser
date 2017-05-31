#valid e program
@ x, y  #declared a and b 
 ^ x = 0 _ x < 20 _ x = x + 2  #for(x = 0; x < 20; x = x + 2) 
 	^ y = 0 _ y < 60 _ y = y + 3  #for(y = 0; y < 60; y = y + 3)
 		!y $  #print y, end for
 !x $ # print x, end for 