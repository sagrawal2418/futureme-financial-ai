import SwiftUI

@main
struct FutureMeFinancialApp: App {
    @StateObject private var viewModel = FutureMeViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: viewModel)
        }
    }
}

