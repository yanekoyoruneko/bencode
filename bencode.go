package bencode

import (
	"bufio"
	"strconv"
)


type BenValue interface {
}

type BenInt struct {
	Num int64
}

type BenString struct {
	Str string
}

type BenDict struct {
	Dict map[string]BenValue
}

type BenList struct {
	List []BenValue
}

func ParseDict(scaner *bufio.Reader) (*BenDict, error) {
	panic("not implemented")
}

func ParseList(scaner *bufio.Reader) (*BenList, error) {
	panic("not implemented")
}

func parseIntUntil(scaner *bufio.Reader, end byte) (int64, error) {
	var buf []byte
	for {
		b, err := scaner.ReadByte()
		if err != nil { return 0, err }
		if b == end { break }
		buf = append(buf, b)
	}
	num, _ := strconv.ParseInt(string(buf), 10, 64)
	return num, nil
}

func ParseString(scaner *bufio.Reader) (*BenString, error) {
	size, err := parseIntUntil(':')
	if err != nil { return nil, err }
	str := make([]byte, size)
	scaner.ReadBytes(size)
	panic("not implemented")
}

func ParseInt(scaner *bufio.Reader) (*BenInt, error) {
	b, err := scaner.ReadByte()
	if err != nil { return nil, err }
	if b != 'i' { return nil, err }
	num, err := parseIntUntil(scaner, 'e')
	if err != nil { return nil, err }
	return &BenInt{ Num: num }, nil
}

func Parse(scaner *bufio.Reader) (BenValue, error) {
	ch, err := scaner.Peek(1)
	if err != nil { return nil, err }
	switch ch[0] {
	case 'd': return ParseDict(scaner)
	case 'l': return ParseList(scaner)
	case 'i': return ParseInt(scaner)
	default:
		if ('0' <= ch[0] && ch[0] <= '9') {
			return ParseString(scaner)
		} else {
			return nil, nil
		}
	}
}
