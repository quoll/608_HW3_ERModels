#!/bin/sh

cut -d , -f 1,2,3,7,12,17,18,19 shopping_trends.csv > customer.csv
cut -d , -f 4,5,8,9 shopping_trends.csv | awk 'NR == 1 {print "id," $0; count = 0; next} !seen[$0]++ {print ++count "," $0}' > item.csv
bb extract-purchase.bb > purchase.csv

