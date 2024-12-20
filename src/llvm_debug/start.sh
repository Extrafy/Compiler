# 分别导出 libsysy 和 main.c 对应的的 .ll 文件
clang -emit-llvm -S ./llvm-test/libsysy.c -o ./llvm-test/lib.ll
clang -emit-llvm -S ./llvm-test/main.c -o ./llvm-test/main.ll

# 使用 llvm-link 将两个文件链接，生成新的 IR 文件
llvm-link ./llvm-test/main.ll ./llvm-test/lib.ll -S -o ./llvm-test/out.ll

# 用 lli 解释运行
lli ./llvm-test/out.ll