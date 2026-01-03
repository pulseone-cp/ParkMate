package at.pulseone.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HistoryFragment : Fragment() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var repository: ParkingTicketRepository
    private lateinit var printingManager: PrintingManager
    private var tickets = mutableListOf<ParkingTicket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        repository = ParkingTicketRepository(requireActivity().application)
        printingManager = PrintingManager(requireContext())

        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)

        historyAdapter = HistoryAdapter(tickets) {
            // Reprint ticket
            printingManager.printTicket(it)
        }
        historyRecyclerView.adapter = historyAdapter

        val searchBar: EditText = view.findViewById(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    val query = s.toString()
                    tickets.clear()
                    if (query.isBlank()) {
                        tickets.addAll(repository.getTickets())
                    } else {
                        tickets.addAll(repository.searchTickets(query))
                    }
                    historyAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        lifecycleScope.launch {
            tickets.addAll(repository.getTickets())
            historyAdapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportHistoryToExcel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportHistoryToExcel() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allTickets = repository.getTickets()
            if (allTickets.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.toast_no_history_to_export, Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(getString(R.string.admin_title_history))

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue(getString(R.string.hint_name))
            headerRow.createCell(1).setCellValue(getString(R.string.hint_surname))
            headerRow.createCell(2).setCellValue(getString(R.string.hint_license_plate))
            headerRow.createCell(3).setCellValue(getString(R.string.hint_department))
            headerRow.createCell(4).setCellValue(getString(R.string.ticket_timestamp_label))

            for ((index, ticket) in allTickets.withIndex()) {
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(ticket.name)
                row.createCell(1).setCellValue(ticket.surname)
                row.createCell(2).setCellValue(ticket.licensePlate)
                row.createCell(3).setCellValue(ticket.department)
                row.createCell(4).setCellValue(ticket.timestamp.toString())
            }

            try {
                val file = File(requireContext().cacheDir, "parking_history.xlsx")
                val fileOut = FileOutputStream(file)
                workbook.write(fileOut)
                fileOut.close()
                workbook.close()

                withContext(Dispatchers.Main) {
                    shareFile(file)
                }

            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.toast_error_exporting_history, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_history_title)))
    }
}