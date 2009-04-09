Function BinaryToString(Binary)
	Dim I,S
	For I = 1 to LenB(Binary)
		S = S & Chr(AscB(MidB(Binary,I,1)))
	Next
	BinaryToString = S
End Function