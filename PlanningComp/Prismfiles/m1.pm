dtmc

module m1
s : [0..1] init 0;
[s0] (s=0) -> 1 : (s'=1);
[s1] (s=1) -> 1 : (s'=0);
endmodule