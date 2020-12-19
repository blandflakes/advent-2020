# Day 1

## Part 1

This could be done by loading everything into memory and doing some nested loops. Seems like it would be faster, though, to do it in one pass - shove everything into a Set. As we go down the line, we know what the other number is, because we subtract it from 2020. So we can check as we read, and if we find the other number in the set, we're done. This isn't necessarily an asymptotic improvement, since in the worst case we still put every element in memory.

## Part 2.

Well damnit. I don't think the Set trick actually works for three numbers - I think the solution basically requires me to nest at least one loop, and requires me to have the numbers in-memory (or to iterate the file several times). So much for elegance. Maybe I can get a very readable for-comprehension on this, once it's all in memory, though. This would actually be a nice time to whip out a kanren implementation.

# Day 2

## Part 1

Lot of string mangling here. Think I'm just going to do the naive "parse everything out" thing and see how it goes. Ok, easy, if lacking in validation.

## Part 2

This seems like I just have to add another validation function...

# Day 3

## Part 1

Ug. I HATE traversal questions. And this one looks like it theoretically wants infinite sequences to the right. Scala has some support for that, though it's not as nice as Clojure. Could
also just calculate how many I'm going to need based on the length of the file. Not sure it's going to be straightforward to do the whole problem within the buffered file... might just be cleaner to load into memory and build nested lazy lists from there.

Couldn't find a way to do any cute reduce, and my parsing code is hilariously inefficient. but it's solved.

## Part 2

Oh man I love when these just generalize (aside from having to promote from Int to Long).

# Day 4

## Part 1

Well, it's not traversal, but there's more janky parsing. Oh well. Let's just rip this stuff into a map and do the dumbest possible validation. Don't want to get too fancy until part 2.

## Part 2

Ug. per-field validation. Not surprising. I could go find a library, or I could just... regex the whole thing? Probably both regex and some validation (don't want to regex year ranges...).

Seems like they could have done more with optional fields - I just ended up not even looking at cid.

# Day 5

## Part 1

Seems like a straightforward binary search, but of course it's really easy to be off by one in many cases. Yup, spent a lot of time to figure out that I needed to step up by 1 when picking the higher range.

## Part 2

This seems... under-specified. I guess the seats probably present in sorted order? So the algorithm looks to be "find the spot in the list where a number is missing, but the surrounding
numbers are there." In other words, look to the spot where the current seat is 2 ids below the next seat?

# Day 6

## Part 1

Well, I guess we can reuse the input parser from the passport thing batch these. Then we just shove everything in a Set? Ok, I put it together, but my answer is wrong. It's... not clear how. AH, found the bug. My batching code can leave lines in the accumulator after the fold.

I've left Day 4 bugged, but fixed this in Day 6.

## Part 2

Well, another elaboration that totally blows up the data structures of Part 1. Need to intersect each individual person and get the count. Probably just a totally parallel set of methods.

Ended up rewriting the code to return lists of batches, and then using some flatten grossness to get the same count. On to part 2. Set intersection to the rescue.

# Day 7

## Part 1

Looks like the knapsack problem or similar. Could be done with dynamic programming, I think. This shouldn't be *too* bad; I think a recursive search of the rules with base cases "empty" (false), "any gold" (true) and memoization should do it. At some point it might be worth abstracting out a simple "mapLines" that calls my contextual "withLines", though most problems with more complicated parsing so far also wanted batches. I guess, actually, that the functional way to do that would be to map to an Option - blank lines get None, valid lines get Some, then we could have split on None. Oh well.

The parser for this is ugly, procedural, java. TODO come back and try one of the attoparsec clones or whatever? It would be nice to have a tool for stuff like this.

I also didn't bother memoizing. Let's see if part 2 would like that.

## Part 2

This was a relatively straightforward solution, and they didn't make the input large enough that I had to worry about runtime. Depth-first search is pretty common in interviews, after all.

Some TODOs that might be interesting, aside from playing with a parsing library:
* make the calls tail recursive
* memoize the calls instead of potentially repeating work

# Day 8

## Part 1

Seems like we always end up writing an interpreter - fun stuff. Interesting to note that it's hitting the same position that is an infinite loop, not the same (structurally) instruction. I did _not_ solve this recursively/functionally, it just didn't feel right.

## Part 2

I figured that part 2 would somehow involve rerunning part 1, but wasn't sure how. I think at this point a small refactoring to make part one more general - it can return something that indicates infinite loop, or something that indicates successful termination (probably just an Either).

Ok, having rewritten this so that it can also handle successful outcomes, I think I just... run the program over and over, mutating a single instruction each time until I get Successful.

I struggled with finding an elegant way to express the mutations. In a way, the fact that they are only temporary (that we only mutate one line, ever) makes it more difficult. Last year's IntCode problem had the "tape" modifying itself, so it was easy to just recursively pass a modified state. In this case, we have to "go back" if the mutation doesn't yield a result.

Thinking about building a lazy sequence of mutations, which would also allow me to flatmap out the instructions that wouldn't mutate (acc).

I'm pretty happy with that outcome. A potential TODO would be to come back and write a functional version of evaluate, but this one looks much more readable as a while loop, IMO.

# Day 9

## Part 1

This one isn't immediately obvious to me. I'm tempted to suggest that a sliding window is going to be useful here, but what do I actually do within the window? I want to be able to ask "are there any two numbers that add to X"?

Can I lazily generate the sliding windows, and calculate all of the possible sums? (That's each number plus each other number). Shouldn't it be possible to only update the window by the thing that slide out and the thing that slid in? We know that worst case is we're going to do all of them.

Maybe this doesn't happen functionally. I could do the inefficient sliding window thing but I feel like I'm just going to be sad. Let's see.

A wc -l reveals that these aren't unique.

Ok, it's getting kinda gross to calculate this window mutably. Let's just do the wasteful window thing where we regenerate the sums every time, in case part 2 screws with us anyway. (Us?)

## Part 2

Huh. I feel there is some elegant solution here but... I'm not sure what it is.

What's the inelegant solution? Try every possible list of contiguous numbers? How does that look?

position 0 until we exceed. Then position 1 until we exceed. That's gross, and we never actually get smaller. Jeez... What's the set of all possible lists?
0-1, 0-2, 0-3... 0-(n-2)
1-2, 2-3, 3-4... 0-(n-2)

Is this right? There is a lot of wasted work there... Can we just generate all possible sums, though?

0 + 1, 0 + 1 + 2, 0 + 1 + 2 + 3
Also, though: 1 + 2, 2 + 3

Ok, so we can start with all pairwise sums, and then add their neighbors, if none of them add up? Or... what if I just do progressively larger sliding windows? We can combine sums over the window very easily. Oh.

Also, today I learned that you can call String.toLong instead of using Long.parseLong.


Ok, weird day. Grossly inefficient on my part, on all fronts.

# Day 10

## Part 1

I feel like I must be missing something that makes this hard... Can't I just sort my input, and then build up a count of differences "up"?

Ok, I'm not missing anything. Weird.

## Part 2

This looks like I need to do a sort of "branching out" in order to identify all possible combinations. So basically, starting at 0, we are going to make a call where we plug in each adapt er within range of 0, and then treat those sublists as the beginning of more recursive calls... ok, shouldn't be too bad.

Turns out it's too bad. Amazing, even at this input size. I guess I basically *have* to DP this. Fine, so how can I do it? I need to build a matrix which counts "connections to here". Then, as I progress down my adapter chain, for each element, I can get all my possible connections, and increment their count by the number of chains to this point.

I'm not pleased with how this one turned out, but I'm pleased that it's done.

# Day 11

## Part 1

Grumble grumble something about stupid grid problems.

## Part 2

Seriously? This is obnoxious.

# Day 12

## Part 1

Taking a page from yesterday's book and continuing to mostly hardcode and repeat operations. Nothing clever here on my part.

## Part 2

Man, I seriously hate positional puzzles. If they keep using these sorts of problems I'm likely to lose motivation very quickly.

This might not be too bad, thankfully. I'll maybe even retrofit some of this to day 1...

Took a while because I had my rotations wacky. Good thing they were always clean rotations instead of arbitrary degrees, though if I'd had to use real formulae I probably wouldn't have made the blunder.

# Day 13

## Part 1

This looks like I just need to simulate the bus routes from time 0 to the time I'm leaving, and take the first departure. I wonder if there is a way to interleave a sequence per bus, or if I need to look up each base at each interval?

Thank god timestamp is just a monotonically increasing int.

## Part 2

I can use a sliding window here. It's going to be slow. There's probably some optimization, like the "Why is grep fast" thing, but maybe if I just wait a while it will find the answer...

Ok great, so this one is not tractable with a naive solution. So there must be some "trick" or optimization to get it done...

I feel like I should just be able to generate the exact number.

Took a lot of beating around the bush to get here. The idea generally being that these patterns should be derivable. They are. Since bus ids are unique, and I think the fact that they're prime plays into this (though not mentioned...), we can say that the pattern would repeat every multiple of them. Problem is that they're offset positionally - that would only solve if we wanted all busses to arrive at once. Fine... so I wrote some gross thing that does the equivalent of aligning some gears:

1. step by stepSize (initially 1) up to a timestamp where adding position is a multiple of the busId (0 is always the first)
2. set the stepSize to stepSize * busId
3. recur to 1

1 is an inner loop.

This is kinda gross, and I had to fumble forever to get any kind of insight.

# Day 14

## Part 1

Well, Scala's bitwise stuff is kinda anemic (so is Java's). I don't know how much work I want to do to be able to work in some kind of "native binary type", though that kind be a fun exercise. I could also isolate the binary work to the mask. Read it in, create an OR mask (this will apply the 1 override) and an AND mask (applying the 0 override). Those can live as whatever kind of number as long as I can use `&` and `^`.

Need to parse batches, first.

I found that to be a massive pain. Oh well, now I'm here, let's see if I can get this star without sobbing about binary representations.

The double mask approach basically wrote itself.

## Part 2

At first blush, maybe I'm not going to get away with that. I'm going to have to get the memory address into a binary string, and then back out... several times. This shouldn't be _too_ awful...

Ok it was kind of annoying, but we got there.

# Day 15

## Part 1

Ok, so what's the structure here. I think I can just make a Map of memory, referring to it when considering my current answer, and adding to it after? I think I just want a usual infinite stream kinda thing.

Ok, not so bad.

## Part 2

One could naively try to run this, but I'm guessing that would be too slow or they wouldn't have gone that route with the challenge?

Oh... nope it just takes a few seconds. I wonder if *only* storing the last two instead of growing the lists makes anything faster. I'm not sure I care enough :)

# Day 16

## Part 1

This seems fairly straightforward. I'll parse these into separate components, but I think I just need to find any number in "nearby tickets" which is not in any of the ranges.

## Part 2

Process of elimination, but how does that actually get encoded?

We can iterate over every ticket, and each value. For each of those values, we're trying to identify which field it is, which actually means eliminating from contention fields which are violated. We want to end up with what? Either an Array[FieldName] or Map[String, Position].

Refactored the restrictions component to be a Map[String, Restriction] for ease of lookup.

Hm. I did a quick and dirty "sum up indexes and then reduce them" situation, but I think it's the case that there isn't guaranteed to be one answer per field? Or rather, there is, but it's not deducible from violations alone. I think there may need to be some backtracking or something when selecting these things.

Ok, fine, so I have to have the candidates and solve, I guess. Wow, this is actually way more complicated. I've done backtracking before, I just... don't want to. Can I do walksat?

Blech. Ok, what do I need? I can get a list of potential fields (strings) for each position. Then I can write a solver that picks one by one, and when it reaches an invalid state, backtracks, until we find a full solution.

Ok, refactored that. Can I do this without backtracking? It may be that while you don't wind up with one candidate per slot, you end up with one slot that has one candidate, and can deduce from there.

Quick check reveals that this... also is not the case. Ok, so we need to support checking multiple options. Can we just create a bunch of combinations and filter out the invalid ones?

Ug. 20 is too large to generate all permutations. FINE. I'll do the backtracking.

Spent less time writing that than I spent avoiding it. Oh well, I would have felt clever had I succeeded.

Looking at discussion boards about this, it feels like I may have missed something? I feel like my validTickets check is correct, but people seem to be able to assume that if you get rid of invalid tickets, you get to the answer...

Yeah super annoyingly I rewrote the naive thing I'd had earlier that just assumes this was deducible without backtracking and it wrked fine. Now I feel even less clever.

# Day 17

# Part 1

Didn't we already do Conway's Game of Life? It's not clear to me why the representation appears to grow. I guess that's because the "outer boundaries" are all 100% inactive prior to the first iteration? No, it's actually not clear to me how the x and y dimensions grow in this puzzle.

This input is incoherent? I guess they must be omitting some "empty lines". I mean this is just Conway's, so I'll try to write something in the morning.

Ok, I got the input represented in some types that seem useful. How do I actually go about solving this? I need to, for all neighbors of all points, do the calculation (otherwise we're only looking at points in our starting grid which is a subset).

Not so bad with a map.

## Part 2

Ok, 4th dimension. Wonder if I can reuse the point or if I should try to generic type this somehow. Just a matter of adding a point and adding neighbors...

Made a CartesianPoint trait, made existing Point Point3, should just be able to pull out and reuse most of the stuff.

Ok, ignoring the ridiculous presentation of input examples, this wasn't so bad.

# Day 18

## Part 1

Hm. Parsing isn't so bad... The numbers in my input are all single digits, so I could do something pretty naive, probably. Wonder if it's easier to do this "correctly" anyway? Or is this an opportunity to use a library?

Trying atto.

... wow that was fun and easy. Oh wait, forget parens entirely.

Not actually that close, either, because I think my associativity is wrong. I was grouping to the right, even before parens, which would make this right associative.

Had to follow along here: https://medium.com/synerise/yet-another-arithmetic-parser-in-scala-43dad055d81f

To get out of my infinite loop when going left-associative. You need to stick a terminal on the left, while passing it into the left-associative grammar to keep left association.

Parens weren't so bad, though.

## Part 2

This *should* just be a matter of reorganizing my parser, but... ouch.

Ok, so I need two levels of evaluation - multiplication has to evaluate prior to addition, which means addition needs to aggregate "prior to" (at a higher level than) multiplication.

Maybe I just make two explicit parsers and try to represent the levels that way?

Ok, I did it, but realized I got it backward - addition is supposed to happen first.

Hm. Actually this is a real pain in the ass. I also don't know that I even like enforcing precedence at this level. But if I'm grouping things associatively, I have to, right? My parse tree basically requires it? Right. And there isn't a way to simplify because left associativity didn't go away - we still have it for the various operations. We *need* to handle addition at a lower level in the parsers.

That took forever. Lessons learned:
1. `parens` from atto will stackoverflow if it forms a recursive definition, and no amount of `delay` will save you.
2. I wasn't parsing correctly because my parsers didn't have a base case to return just an Argument if no addition was present. There is also a pretty rough naming problem - mult and add aren't correct terms, but they somewhat conveyed the dual layers I was trying to hit.

I liked writing the interpeter, but the parser was kinda painful.
