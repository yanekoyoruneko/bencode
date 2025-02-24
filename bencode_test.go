package bencode

import (
	"testing"
	"os"
	"bufio"
)

// Test function must start with "Test"
func TestEncode(t *testing.T) {
	file, err := os.Open("test")
	if err != nil {
		panic("e")
	}
	scaner := bufio.NewReader(file)
	bval, err := Parse(scaner)
	if err != nil {
		panic("e")
	}
	switch v := bval.(type) {
	case *BenInt:
		t.Log("Value", v.Num)
	default:
		panic("xdd")
	}
	return
}
