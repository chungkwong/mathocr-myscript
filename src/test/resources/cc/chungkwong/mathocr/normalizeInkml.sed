# Use the following command to normalize CROHME 2014 test set
#     sed -i -E -f normalizeInkml.sed *.inkml
s/>\\(sum|int|prime|pm|div|times|lim|sin|cos|tan|log|exists|forall|leq|alpha|beta|gamma|lambda|omega|phi|pi|theta|sigma|mu|Delta)</>\1</g
s/>\\infty</>infin</g
s/>\\rightarrow</>rarr</g
s/>\\ldots</>hellip</g
s/>le</>leq</g
s/>\\geq</>ge</g
s/>geq</>ge</g
s/>gt</>></g
s/>\\gt</>></g
s/>\\lt</>&lt\;</g
s/>lt</>&lt\;</g
s/>neq</>ne</g
s/>\\neq</>ne</g