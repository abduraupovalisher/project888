// MinMax.asm

// MinMax.min
// Function to find the minimum of two values
(MinMax.min)
    push argument 0       // Push the first argument (x) onto the stack
    pop temp 0            // Store x in temp[0]
    push argument 1       // Push the second argument (y) onto the stack
    pop temp 1            // Store y in temp[1]
    // Compare x and y
    push temp 0           // Push x (stored in temp[0])
    push temp 1           // Push y (stored in temp[1])
    sub                   // Subtract (x - y)
    jge MIN               // If x >= y (x - y >= 0), jump to MIN
    push temp 0           // Else, x is smaller, so push x
    return

(MIN)
    push temp 1           // y is smaller, push y
    return


// MinMax.max
// Function to find the maximum of two values
(MinMax.max)
    push argument 0       // Push the first argument (x) onto the stack
    pop temp 0            // Store x in temp[0]
    push argument 1       // Push the second argument (y) onto the stack
    pop temp 1            // Store y in temp[1]
    // Compare x and y
    push temp 0           // Push x (stored in temp[0])
    push temp 1           // Push y (stored in temp[1])
    sub                   // Subtract (x - y)
    jle MAX               // If x <= y (x - y <= 0), jump to MAX
    push temp 0           // Else, x is larger, so push x
    return

(MAX)
    push temp 1           // y is larger, push y
    return


// MinMax.main
// Main function to test the min and max functions
(MinMax.main)
    push constant 10      // Push constant 10 (first argument)
    push constant 20      // Push constant 20 (second argument)
    call MinMax.min 2     // Call MinMax.min with 2 arguments (should return 10)
    pop temp 0            // Store the result of min in temp[0]

    push constant 10      // Push constant 10 (first argument)
    push constant 20      // Push constant 20 (second argument)
    call MinMax.max 2     // Call MinMax.max with 2 arguments (should return 20)
    pop temp 1            // Store the result of max in temp[1]

    return                // End the main function
