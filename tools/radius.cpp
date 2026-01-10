#include <bits/stdc++.h>

using namespace std;

int main() {
    const int w = 6;
    int rad2; cin >> rad2;

    for (int r = -w; r <= w; r++) {
        for (int c = -w; c <= w; c++) {
            if (r * r + c * c <= rad2) {
                cout << '#';
            } else {
                cout << '.';
            }
        }
        cout << '\n';
    }
}