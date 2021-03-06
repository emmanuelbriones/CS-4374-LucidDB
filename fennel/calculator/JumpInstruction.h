/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2004 SQLstream, Inc.
// Copyright (C) 2009 Dynamo BI Corporation
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Jump Instruction
//
// Instruction->Jump
*/
#ifndef Fennel_JumpInstruction_Included
#define Fennel_JumpInstruction_Included

#include "fennel/calculator/Instruction.h"

FENNEL_BEGIN_NAMESPACE

class FENNEL_CALCULATOR_EXPORT JumpInstruction
    : public Instruction
{
public:
    explicit
    JumpInstruction(TProgramCounter pc) : mJumpTo(pc), mOp() {}

    explicit
    JumpInstruction(
        TProgramCounter pc,
        RegisterRef<bool>* op)
        : mJumpTo(pc), mOp(op)
    {}

    virtual
    ~JumpInstruction() {}

protected:
    TProgramCounter mJumpTo;
    RegisterRef<bool>* mOp;     // may be unused

    virtual void describeHelper(
        string &out,
        bool values,
        const char* longName,
        const char* shortName) const;
};

class FENNEL_CALCULATOR_EXPORT Jump
    : public JumpInstruction
{
public:
    explicit
    Jump(TProgramCounter pc)
        : JumpInstruction(pc)
    {}

    virtual
    ~Jump() {}

    virtual void exec(TProgramCounter& pc) const {
        pc = mJumpTo;
    }

    static const char * longName();
    static const char * shortName();
    static int numArgs();
    void describe(string& out, bool values) const;

    static InstructionSignature
    signature(StandardTypeDescriptorOrdinal type) {
        vector<StandardTypeDescriptorOrdinal> v(numArgs(), type);
        return InstructionSignature(shortName(), 0, v);
    }

    static Instruction*
    create(InstructionSignature const & sig)
    {
        assert(sig.size() == numArgs());
        return new Jump(sig.getPc());
    }
};

class FENNEL_CALCULATOR_EXPORT JumpTrue
    : public JumpInstruction
{
public:
    explicit
    JumpTrue(TProgramCounter pc, RegisterRef<bool>* op)
        : JumpInstruction(pc, op)
    {}

    virtual
    ~JumpTrue() {}

    virtual void exec(TProgramCounter& pc) const {
        if (!mOp->isNull() && mOp->value() == true) {
            pc = mJumpTo;
        } else {
            pc++;
        }
    }

    static const char * longName();
    static const char * shortName();
    static int numArgs();
    void describe(string& out, bool values) const;

    static InstructionSignature
    signature(StandardTypeDescriptorOrdinal type) {
        vector<StandardTypeDescriptorOrdinal> v(numArgs(), type);
        return InstructionSignature(shortName(), 0, v);
    }

    static Instruction*
    create(InstructionSignature const & sig)
    {
        assert(sig.size() == numArgs());
        return new JumpTrue(
            sig.getPc(),
            static_cast<RegisterRef<bool>*> (sig[0]));
    }
};

class FENNEL_CALCULATOR_EXPORT JumpFalse
    : public JumpInstruction
{
public:
    explicit
    JumpFalse(TProgramCounter pc, RegisterRef<bool>* op)
        : JumpInstruction(pc, op)
    {}

    virtual
    ~JumpFalse() {}

    virtual void exec(TProgramCounter& pc) const {
        if (!mOp->isNull() && mOp->value() == false) {
            pc = mJumpTo;
        } else {
            pc++;
        }
    }

    static const char * longName();
    static const char * shortName();
    static int numArgs();
    void describe(string& out, bool values) const;

    static InstructionSignature
    signature(StandardTypeDescriptorOrdinal type) {
        vector<StandardTypeDescriptorOrdinal> v(numArgs(), type);
        return InstructionSignature(shortName(), 0, v);
    }

    static Instruction*
    create(InstructionSignature const & sig)
    {
        assert(sig.size() == numArgs());
        return new JumpFalse(
            sig.getPc(),
            static_cast<RegisterRef<bool>*> (sig[0]));
    }

};

class FENNEL_CALCULATOR_EXPORT JumpNull
    : public JumpInstruction
{
public:
    explicit
    JumpNull(TProgramCounter pc, RegisterRef<bool>* op)
        : JumpInstruction(pc, op)
    {}

    virtual
    ~JumpNull() {}

    virtual void exec(TProgramCounter& pc) const {
        if (mOp->isNull()) {
            pc = mJumpTo;
        } else {
            pc++;
        }
    }

    static const char * longName();
    static const char * shortName();
    static int numArgs();
    void describe(string& out, bool values) const;

    static InstructionSignature
    signature(StandardTypeDescriptorOrdinal type) {
        vector<StandardTypeDescriptorOrdinal> v(numArgs(), type);
        return InstructionSignature(shortName(), 0, v);
    }

    static Instruction*
    create(InstructionSignature const & sig)
    {
        assert(sig.size() == numArgs());
        return new JumpNull(
            sig.getPc(),
            static_cast<RegisterRef<bool>*> (sig[0]));
    }

};

class FENNEL_CALCULATOR_EXPORT JumpNotNull
    : public JumpInstruction
{
public:
    explicit
    JumpNotNull(TProgramCounter pc, RegisterRef<bool>* op)
        : JumpInstruction(pc, op)
    {}

    virtual
    ~JumpNotNull() {}

    virtual void exec(TProgramCounter& pc) const {
        if (!mOp->isNull()) {
            pc = mJumpTo;
        } else {
            pc++;
        }
    }

    static const char * longName();
    static const char * shortName();
    static int numArgs();
    void describe(string& out, bool values) const;

    static InstructionSignature
    signature(StandardTypeDescriptorOrdinal type) {
        vector<StandardTypeDescriptorOrdinal> v(numArgs(), type);
        return InstructionSignature(shortName(), 0, v);
    }

    static Instruction*
    create(InstructionSignature const & sig)
    {
        assert(sig.size() == numArgs());
        return new JumpNotNull(
            sig.getPc(),
            static_cast<RegisterRef<bool>*> (sig[0]));
    }

};

class FENNEL_CALCULATOR_EXPORT JumpInstructionRegister
    : InstructionRegister
{
    // TODO: Refactor registerTypes to class InstructionRegister
    template < class INSTCLASS2 >
    static void
    registerTypes(vector<StandardTypeDescriptorOrdinal> const &t) {
        for (uint i = 0; i < t.size(); i++) {
            StandardTypeDescriptorOrdinal type = t[i];
            InstructionSignature sig = INSTCLASS2::signature(type);
            switch (type) {
#define Fennel_InstructionRegisterSwitch_Bool 1
#include "fennel/calculator/InstructionRegisterSwitch.h"
            default:
                throw std::logic_error("Default InstructionRegister");
            }
        }
    }

public:
    static void
    registerInstructions() {
        vector<StandardTypeDescriptorOrdinal> t;
        t.push_back(STANDARD_TYPE_BOOL);

        // Have to do full fennel:: qualification of template
        // arguments below to prevent template argument 'TMPLT', of
        // this encapsulating class, from perverting NativeAdd into
        // NativeAdd<TMPLT> or something like
        // that. Anyway. Fennel::NativeAdd works just fine.
        registerTypes<fennel::Jump>(t);
        registerTypes<fennel::JumpTrue>(t);
        registerTypes<fennel::JumpFalse>(t);
        registerTypes<fennel::JumpNull>(t);
        registerTypes<fennel::JumpNotNull>(t);
    }
};


FENNEL_END_NAMESPACE

#endif

// End JumpInstruction.h

